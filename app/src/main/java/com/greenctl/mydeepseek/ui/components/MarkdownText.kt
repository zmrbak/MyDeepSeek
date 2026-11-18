package com.greenctl.mydeepseek.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenctl.mydeepseek.data.Markdown
import com.greenctl.mydeepseek.data.MdBlock
import com.greenctl.mydeepseek.ui.theme.DsTheme

private val INLINE_TOKEN = Regex("(\\*\\*[^*\\n]+\\*\\*|`[^`\\n]+`)")

fun buildInlineText(
    text: String,
    boldColor: Color,
    codeColor: Color,
    codeBackground: Color,
    baseWeight: FontWeight = FontWeight.Normal,
): AnnotatedString = buildAnnotatedString {
    var lastIndex = 0
    INLINE_TOKEN.findAll(text).forEach { match ->
        append(text.substring(lastIndex, match.range.first))
        val token = match.value
        when {
            token.startsWith("**") -> {
                pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = boldColor))
                append(token.removeSurrounding("**"))
                pop()
            }

            token.startsWith("`") -> {
                pushStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        color = codeColor,
                        background = codeBackground,
                        fontSize = 13.sp,
                    )
                )
                append(token.removeSurrounding("`"))
                pop()
            }
        }
        lastIndex = match.range.last + 1
    }
    if (lastIndex < text.length) append(text.substring(lastIndex))
}

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    lineHeight: TextUnit = 26.sp,
) {
    val colors = DsTheme.colors
    val blocks = remember(text) { Markdown.parse(text) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        blocks.forEach { block ->
            when (block) {
                is MdBlock.Paragraph -> Text(
                    text = buildInlineText(
                        block.text,
                        boldColor = colors.textPrimary,
                        codeColor = colors.textPrimary,
                        codeBackground = colors.elevated,
                    ),
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    color = colors.textPrimary,
                )

                is MdBlock.Heading -> Text(
                    text = buildInlineText(
                        block.text,
                        boldColor = colors.textPrimary,
                        codeColor = colors.textPrimary,
                        codeBackground = colors.elevated,
                    ),
                    fontSize = (fontSize.value + 2).sp,
                    lineHeight = (lineHeight.value + 4).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                )

                is MdBlock.Bullets -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    block.items.forEach { item ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "•",
                                fontSize = fontSize,
                                lineHeight = lineHeight,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(end = 8.dp),
                            )
                            Text(
                                text = buildInlineText(
                                    item,
                                    boldColor = colors.textPrimary,
                                    codeColor = colors.textPrimary,
                                    codeBackground = colors.elevated,
                                ),
                                fontSize = fontSize,
                                lineHeight = lineHeight,
                                color = colors.textPrimary,
                            )
                        }
                    }
                }

                is MdBlock.Numbered -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    block.items.forEachIndexed { index, item ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "${index + 1}.",
                                fontSize = fontSize,
                                lineHeight = lineHeight,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(end = 8.dp),
                            )
                            Text(
                                text = buildInlineText(
                                    item,
                                    boldColor = colors.textPrimary,
                                    codeColor = colors.textPrimary,
                                    codeBackground = colors.elevated,
                                ),
                                fontSize = fontSize,
                                lineHeight = lineHeight,
                                color = colors.textPrimary,
                            )
                        }
                    }
                }

                is MdBlock.Quote -> Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(
                        modifier = Modifier
                            .width(3.dp)
                            .height((lineHeight.value * 1.4f).dp)
                            .background(colors.divider, RoundedCornerShape(2.dp))
                    )
                    Text(
                        text = buildInlineText(
                            block.text,
                            boldColor = colors.textSecondary,
                            codeColor = colors.textSecondary,
                            codeBackground = colors.elevated,
                        ),
                        fontSize = (fontSize.value - 1).sp,
                        lineHeight = lineHeight,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }

                is MdBlock.Code -> CodeBlock(block.language, block.code, fontSize)
            }
        }
    }
}

@Composable
private fun CodeBlock(language: String, code: String, fontSize: TextUnit) {
    val colors = DsTheme.colors
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.codeBackground),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.codeHeader)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = language.ifBlank { "代码" },
                fontSize = 12.sp,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.elevated)
                    .clickableNoRipple {
                        copyToClipboard(context, "代码", code)
                        Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                DsIcon(DsIconType.Copy, colors.textSecondary, size = 13.dp, strokeWidth = 1.6.dp)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "复制",
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                )
            }
        }
        Text(
            text = code,
            fontSize = (fontSize.value - 2).sp,
            lineHeight = (fontSize.value + 6).sp,
            fontFamily = FontFamily.Monospace,
            color = colors.codeText,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
        )
    }
}

fun copyToClipboard(context: Context, label: String, text: String = label) {
    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    manager.setPrimaryClip(ClipData.newPlainText(label, text))
}
