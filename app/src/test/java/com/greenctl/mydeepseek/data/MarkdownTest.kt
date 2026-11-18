package com.greenctl.mydeepseek.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownTest {

    @Test
    fun parsesFencedCodeBlockWithLanguage() {
        val blocks = Markdown.parse("说明：\n```kotlin\nval a = 1\n```\n结束")
        assertEquals(3, blocks.size)
        assertTrue(blocks[0] is MdBlock.Paragraph)
        val code = blocks[1] as MdBlock.Code
        assertEquals("kotlin", code.language)
        assertEquals("val a = 1", code.code)
        assertEquals("结束", (blocks[2] as MdBlock.Paragraph).text)
    }

    @Test
    fun keepsUnclosedCodeBlockWhileStreaming() {
        val blocks = Markdown.parse("```python\nprint(1)")
        assertEquals(1, blocks.size)
        val code = blocks[0] as MdBlock.Code
        assertEquals("python", code.language)
        assertEquals("print(1)", code.code)
    }

    @Test
    fun parsesHeadingAndBullets() {
        val blocks = Markdown.parse("# 标题\n- 第一项\n- 第二项")
        assertEquals(2, blocks.size)
        assertEquals("标题", (blocks[0] as MdBlock.Heading).text)
        assertEquals(listOf("第一项", "第二项"), (blocks[1] as MdBlock.Bullets).items)
    }

    @Test
    fun parsesNumberedList() {
        val blocks = Markdown.parse("1. 打开设置\n2. 输入 Key")
        assertEquals(1, blocks.size)
        assertEquals(listOf("打开设置", "输入 Key"), (blocks[0] as MdBlock.Numbered).items)
    }

    @Test
    fun mergesConsecutiveLinesIntoParagraph() {
        val blocks = Markdown.parse("第一行\n第二行")
        assertEquals(1, blocks.size)
        assertEquals("第一行\n第二行", (blocks[0] as MdBlock.Paragraph).text)
    }

    @Test
    fun parsesQuote() {
        val blocks = Markdown.parse("> 引用内容")
        assertEquals(1, blocks.size)
        assertEquals("引用内容", (blocks[0] as MdBlock.Quote).text)
    }

    @Test
    fun emptySourceProducesNoBlocks() {
        assertTrue(Markdown.parse("").isEmpty())
        assertTrue(Markdown.parse("\n\n").isEmpty())
    }
}
