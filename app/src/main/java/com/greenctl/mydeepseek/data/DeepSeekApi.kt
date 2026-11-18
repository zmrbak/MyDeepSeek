package com.greenctl.mydeepseek.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class ApiException(message: String) : Exception(message)

/** 发送给模型的一轮对话。 */
data class ApiTurn(val role: String, val content: String)

/**
 * DeepSeek 开放平台 /chat/completions 的流式客户端，使用 HttpURLConnection，不引入额外依赖。
 */
class DeepSeekApi {

    @Volatile
    private var activeConnection: HttpURLConnection? = null

    fun cancel() {
        runCatching { activeConnection?.disconnect() }
        activeConnection = null
    }

    suspend fun streamChat(
        baseUrl: String,
        apiKey: String,
        model: String,
        turns: List<ApiTurn>,
        onDelta: (content: String, reasoning: String) -> Unit,
    ) = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) throw ApiException(ERR_NO_KEY)
        val endpoint = normalizeBaseUrl(baseUrl) + "/chat/completions"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 180_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "text/event-stream")
            setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
        }
        activeConnection = connection
        try {
            val body = buildRequestBody(model, turns).toString()
            connection.outputStream.use { it.write(body.toByteArray(StandardCharsets.UTF_8)) }

            val code = connection.responseCode
            if (code !in 200..299) {
                val message = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                throw ApiException(describeHttpError(code, message))
            }

            val reader = BufferedReader(InputStreamReader(connection.inputStream, StandardCharsets.UTF_8))
            reader.use {
                while (true) {
                    if (!currentCoroutineContext().isActive) throw CancellationException("cancelled")
                    val line = it.readLine() ?: break
                    if (!line.startsWith("data:")) continue
                    val payload = line.removePrefix("data:").trim()
                    if (payload.isEmpty()) continue
                    if (SseParser.isDone(payload)) break
                    val delta = SseParser.parseData(payload)
                    if (delta.content.isNotEmpty() || delta.reasoning.isNotEmpty()) {
                        onDelta(delta.content, delta.reasoning)
                    }
                }
            }
        } catch (e: ApiException) {
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw ApiException(e.message ?: ERR_NETWORK)
        } finally {
            activeConnection = null
            runCatching { connection.disconnect() }
        }
    }

    companion object {
        const val ERR_NO_KEY = "请先在设置中填写 DeepSeek API Key"
        const val ERR_NETWORK = "网络异常，请检查你的网络状况"
        const val ERR_SERVER = "当前服务异常，请稍后再试"

        fun normalizeBaseUrl(baseUrl: String): String {
            val trimmed = baseUrl.trim().ifBlank { "https://api.deepseek.com" }
            return trimmed.trimEnd('/')
        }

        fun buildRequestBody(model: String, turns: List<ApiTurn>): JSONObject {
            val messages = JSONArray()
            turns.forEach { turn ->
                messages.put(
                    JSONObject()
                        .put("role", turn.role)
                        .put("content", turn.content)
                )
            }
            return JSONObject()
                .put("model", model)
                .put("messages", messages)
                .put("stream", true)
        }

        fun describeHttpError(code: Int, rawBody: String): String = when (code) {
            401 -> "API Key 无效，请检查后重试"
            402 -> "账户余额不足，请前往开放平台充值"
            429 -> "请求过于频繁，请稍后再试"
            else -> {
                val detail = runCatching {
                    JSONObject(rawBody).optJSONObject("error")?.optString("message").orEmpty()
                }.getOrDefault("")
                if (detail.isNotBlank()) "$ERR_SERVER（$detail）" else "$ERR_SERVER（$code）"
            }
        }
    }
}

/** SSE 数据行解析，独立出来便于单元测试。 */
object SseParser {

    data class Delta(val content: String, val reasoning: String)

    fun isDone(payload: String): Boolean = payload.trim() == "[DONE]"

    fun parseData(payload: String): Delta {
        val json = runCatching { JSONObject(payload) }.getOrNull() ?: return Delta("", "")
        val choices = json.optJSONArray("choices") ?: return Delta("", "")
        if (choices.length() == 0) return Delta("", "")
        val delta = choices.optJSONObject(0)?.optJSONObject("delta") ?: return Delta("", "")
        return Delta(
            content = delta.optString("content", ""),
            reasoning = delta.optString("reasoning_content", ""),
        )
    }
}
