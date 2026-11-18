package com.greenctl.mydeepseek.data

import androidx.compose.runtime.Immutable
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class Role { USER, ASSISTANT }

enum class MessageState { STREAMING, DONE, STOPPED, ERROR }

/** 联网搜索步骤的状态，用于复刻「正在搜索 / 联网搜索暂不可用」的工具行。 */
enum class SearchState { NONE, SEARCHING, UNAVAILABLE }

@Immutable
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: Role,
    val content: String = "",
    val reasoning: String = "",
    val thinkMillis: Long = 0L,
    val state: MessageState = MessageState.DONE,
    val error: String? = null,
    // 0 无反馈，1 喜欢，-1 不喜欢
    val feedback: Int = 0,
    val search: SearchState = SearchState.NONE,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val isStreaming: Boolean get() = state == MessageState.STREAMING
}

@Immutable
data class ChatSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "新对话",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val pinned: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
) {
    val preview: String
        get() = messages.lastOrNull { it.role == Role.USER }?.content?.lineSequence()?.firstOrNull().orEmpty()
}

enum class DsModel(
    val label: String,
    val description: String,
    val apiModel: String,
) {
    FAST("快速模式", "适合日常对话，即时响应", "deepseek-chat"),
    EXPERT("专家模式", "擅长复杂问题，高峰需等待", "deepseek-reasoner"),
    ;

    companion object {
        fun of(name: String?): DsModel = entries.firstOrNull { it.name == name } ?: FAST
    }
}

@Immutable
data class Attachment(
    val name: String,
    val text: String,
    val sizeBytes: Long = text.length.toLong(),
)

@Immutable
data class AppSettings(
    // 0 跟随系统，1 浅色，2 深色
    val appearance: Int = 0,
    // 0 小，1 标准，2 大
    val fontScale: Int = 1,
    val apiKey: String = "",
    val baseUrl: String = "https://api.deepseek.com",
    val thinkingAutoFold: Boolean = true,
    val autoReadAloud: Boolean = false,
    val deepThink: Boolean = false,
    val webSearch: Boolean = false,
    val modelName: String = DsModel.FAST.name,
    val lastSessionId: String? = null,
) {
    val textScale: Float
        get() = when (fontScale) {
            0 -> 0.9f
            2 -> 1.12f
            else -> 1f
        }
}

// ---------- JSON 序列化（使用系统内置 org.json，无需额外依赖） ----------

fun ChatMessage.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("role", role.name)
    put("content", content)
    put("reasoning", reasoning)
    put("thinkMillis", thinkMillis)
    put("state", state.name)
    error?.let { put("error", it) }
    put("feedback", feedback)
    put("search", search.name)
    put("createdAt", createdAt)
}

fun messageFromJson(json: JSONObject): ChatMessage = ChatMessage(
    id = json.optString("id", UUID.randomUUID().toString()),
    role = runCatching { Role.valueOf(json.optString("role", Role.USER.name)) }.getOrDefault(Role.USER),
    content = json.optString("content", ""),
    reasoning = json.optString("reasoning", ""),
    thinkMillis = json.optLong("thinkMillis", 0L),
    state = runCatching { MessageState.valueOf(json.optString("state", MessageState.DONE.name)) }
        .getOrDefault(MessageState.DONE),
    error = json.optString("error", "").ifBlank { null },
    feedback = json.optInt("feedback", 0),
    search = runCatching { SearchState.valueOf(json.optString("search", SearchState.NONE.name)) }
        .getOrDefault(SearchState.NONE),
    createdAt = json.optLong("createdAt", System.currentTimeMillis()),
)

fun ChatSession.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("title", title)
    put("createdAt", createdAt)
    put("updatedAt", updatedAt)
    put("pinned", pinned)
    put("messages", JSONArray().apply { messages.forEach { put(it.toJson()) } })
}

fun sessionFromJson(json: JSONObject): ChatSession {
    val messages = mutableListOf<ChatMessage>()
    val array = json.optJSONArray("messages")
    if (array != null) {
        for (i in 0 until array.length()) {
            array.optJSONObject(i)?.let { messages.add(messageFromJson(it)) }
        }
    }
    return ChatSession(
        id = json.optString("id", UUID.randomUUID().toString()),
        title = json.optString("title", "新对话"),
        createdAt = json.optLong("createdAt", System.currentTimeMillis()),
        updatedAt = json.optLong("updatedAt", System.currentTimeMillis()),
        pinned = json.optBoolean("pinned", false),
        messages = messages,
    )
}

fun AppSettings.toJson(): JSONObject = JSONObject().apply {
    put("appearance", appearance)
    put("fontScale", fontScale)
    put("apiKey", apiKey)
    put("baseUrl", baseUrl)
    put("thinkingAutoFold", thinkingAutoFold)
    put("autoReadAloud", autoReadAloud)
    put("deepThink", deepThink)
    put("webSearch", webSearch)
    put("modelName", modelName)
    lastSessionId?.let { put("lastSessionId", it) }
}

fun settingsFromJson(json: JSONObject): AppSettings = AppSettings(
    appearance = json.optInt("appearance", 0),
    fontScale = json.optInt("fontScale", 1),
    apiKey = json.optString("apiKey", ""),
    baseUrl = json.optString("baseUrl", "https://api.deepseek.com"),
    thinkingAutoFold = json.optBoolean("thinkingAutoFold", true),
    autoReadAloud = json.optBoolean("autoReadAloud", false),
    deepThink = json.optBoolean("deepThink", false),
    webSearch = json.optBoolean("webSearch", false),
    modelName = json.optString("modelName", DsModel.FAST.name),
    lastSessionId = json.optString("lastSessionId", "").ifBlank { null },
)
