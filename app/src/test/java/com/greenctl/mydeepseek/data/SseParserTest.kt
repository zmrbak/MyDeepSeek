package com.greenctl.mydeepseek.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SseParserTest {

    @Test
    fun parsesContentDelta() {
        val payload = """{"id":"1","choices":[{"delta":{"content":"你好"},"index":0}]}"""
        val delta = SseParser.parseData(payload)
        assertEquals("你好", delta.content)
        assertEquals("", delta.reasoning)
    }

    @Test
    fun parsesReasoningDelta() {
        val payload = """{"choices":[{"delta":{"reasoning_content":"先分析一下"}}]}"""
        val delta = SseParser.parseData(payload)
        assertEquals("", delta.content)
        assertEquals("先分析一下", delta.reasoning)
    }

    @Test
    fun parsesRoleOnlyDeltaAsEmpty() {
        val payload = """{"choices":[{"delta":{"role":"assistant"}}]}"""
        val delta = SseParser.parseData(payload)
        assertEquals("", delta.content)
        assertEquals("", delta.reasoning)
    }

    @Test
    fun detectsDoneMarker() {
        assertTrue(SseParser.isDone(" [DONE] "))
        assertFalse(SseParser.isDone("""{"choices":[]}"""))
    }

    @Test
    fun malformedJsonYieldsEmptyDelta() {
        val delta = SseParser.parseData("not-json")
        assertEquals("", delta.content)
        assertEquals("", delta.reasoning)
    }

    @Test
    fun emptyChoicesYieldsEmptyDelta() {
        val delta = SseParser.parseData("""{"choices":[]}""")
        assertEquals("", delta.content)
    }

    @Test
    fun requestBodyContainsModelAndStream() {
        val body = DeepSeekApi.buildRequestBody(
            model = "deepseek-reasoner",
            turns = listOf(ApiTurn("user", "你好")),
        )
        assertEquals("deepseek-reasoner", body.getString("model"))
        assertTrue(body.getBoolean("stream"))
        val messages = body.getJSONArray("messages")
        assertEquals(1, messages.length())
        assertEquals("user", messages.getJSONObject(0).getString("role"))
        assertEquals("你好", messages.getJSONObject(0).getString("content"))
    }

    @Test
    fun normalizesBaseUrl() {
        assertEquals("https://api.deepseek.com", DeepSeekApi.normalizeBaseUrl("https://api.deepseek.com/"))
        assertEquals("https://api.deepseek.com", DeepSeekApi.normalizeBaseUrl("  "))
    }

    @Test
    fun describesHttpErrors() {
        assertEquals("API Key 无效，请检查后重试", DeepSeekApi.describeHttpError(401, ""))
        assertEquals("请求过于频繁，请稍后再试", DeepSeekApi.describeHttpError(429, ""))
        val server = DeepSeekApi.describeHttpError(500, """{"error":{"message":"boom"}}""")
        assertTrue(server.contains("boom"))
    }
}
