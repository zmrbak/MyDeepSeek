package com.greenctl.mydeepseek.data

/**
 * 极简 Markdown 块级解析，用于把模型输出渲染成文档化界面。
 * 支持：标题、无序/有序列表、引用、围栏代码块、段落。
 */
sealed interface MdBlock {
    data class Paragraph(val text: String) : MdBlock
    data class Heading(val text: String) : MdBlock
    data class Bullets(val items: List<String>) : MdBlock
    data class Numbered(val items: List<String>) : MdBlock
    data class Quote(val text: String) : MdBlock
    data class Code(val language: String, val code: String) : MdBlock
}

object Markdown {

    fun parse(source: String): List<MdBlock> {
        val blocks = mutableListOf<MdBlock>()
        val lines = source.replace("\r\n", "\n").split("\n")
        val paragraph = StringBuilder()
        val bullets = mutableListOf<String>()
        val numbered = mutableListOf<String>()
        val quote = StringBuilder()

        fun flushParagraph() {
            if (paragraph.isNotBlank()) {
                blocks.add(MdBlock.Paragraph(paragraph.toString().trim()))
                paragraph.clear()
            }
        }

        fun flushBullets() {
            if (bullets.isNotEmpty()) {
                blocks.add(MdBlock.Bullets(bullets.toList()))
                bullets.clear()
            }
        }

        fun flushNumbered() {
            if (numbered.isNotEmpty()) {
                blocks.add(MdBlock.Numbered(numbered.toList()))
                numbered.clear()
            }
        }

        fun flushQuote() {
            if (quote.isNotBlank()) {
                blocks.add(MdBlock.Quote(quote.toString().trim()))
                quote.clear()
            }
        }

        fun flushAll() {
            flushParagraph()
            flushBullets()
            flushNumbered()
            flushQuote()
        }

        var index = 0
        while (index < lines.size) {
            val raw = lines[index]
            val line = raw.trimEnd()

            if (line.trimStart().startsWith("```")) {
                flushAll()
                val language = line.trim().removePrefix("```").trim()
                val code = StringBuilder()
                index++
                while (index < lines.size && !lines[index].trimStart().startsWith("```")) {
                    if (code.isNotEmpty()) code.append('\n')
                    code.append(lines[index])
                    index++
                }
                // 流式输出时可能还没有结束围栏，同样按代码块渲染
                blocks.add(MdBlock.Code(language, code.toString()))
                index++
                continue
            }

            val trimmed = line.trimStart()
            when {
                line.isBlank() -> flushAll()
                trimmed.startsWith("#") -> {
                    flushAll()
                    blocks.add(MdBlock.Heading(trimmed.trimStart('#').trim()))
                }
                trimmed.startsWith("> ") || trimmed == ">" -> {
                    flushParagraph()
                    flushBullets()
                    flushNumbered()
                    if (quote.isNotEmpty()) quote.append('\n')
                    quote.append(trimmed.removePrefix(">").trim())
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("+ ") -> {
                    flushParagraph()
                    flushNumbered()
                    flushQuote()
                    bullets.add(trimmed.substring(2))
                }
                NUMBERED.containsMatchIn(trimmed) -> {
                    flushParagraph()
                    flushBullets()
                    flushQuote()
                    numbered.add(NUMBERED.replace(trimmed, ""))
                }
                else -> {
                    flushBullets()
                    flushNumbered()
                    flushQuote()
                    if (paragraph.isNotEmpty()) paragraph.append('\n')
                    paragraph.append(trimmed)
                }
            }
            index++
        }
        flushAll()
        return blocks
    }

    private val NUMBERED = Regex("^\\d{1,2}\\.\\s+")
}
