package com.greenctl.mydeepseek.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenctl.mydeepseek.data.ChatMessage
import com.greenctl.mydeepseek.data.MessageState
import com.greenctl.mydeepseek.data.SearchState
import com.greenctl.mydeepseek.ui.theme.DsTheme
import kotlin.math.roundToInt

@Composable
fun MessageItem(
    message: ChatMessage,
    textScale: Float,
    thinkingAutoFold: Boolean,
    onRegenerate: () -> Unit,
    onFeedback: (Int) -> Unit,
    onReadAloud: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DsTheme.colors
    val context = LocalContext.current
    val bodySize = (16f * textScale).sp
    val bodyLine = (26f * textScale).sp

    if (message.role == com.greenctl.mydeepseek.data.Role.USER) {
        val maxWidth = (LocalConfiguration.current.screenWidthDp * 0.78f).dp
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            Surface(
                color = colors.bubble,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.widthIn(max = maxWidth),
            ) {
                Text(
                    text = message.content,
                    fontSize = bodySize,
                    lineHeight = bodyLine,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                )
            }
        }
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        AssistantAvatar(size = 26.dp)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            when (message.search) {
                SearchState.SEARCHING -> ToolRow(text = "正在搜索", animated = true)
                SearchState.UNAVAILABLE -> ToolRow(text = "联网搜索暂不可用")
                SearchState.NONE -> Unit
            }

            if (message.reasoning.isNotBlank() || (message.isStreaming && message.content.isBlank())) {
                if (message.reasoning.isNotBlank()) {
                    ThinkingBlock(
                        reasoning = message.reasoning,
                        thinkMillis = message.thinkMillis,
                        streaming = message.isStreaming,
                        autoFold = thinkingAutoFold,
                        textScale = textScale,
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }

            if (message.content.isNotBlank()) {
                MarkdownText(
                    text = message.content,
                    fontSize = bodySize,
                    lineHeight = bodyLine,
                )
            } else if (message.isStreaming) {
                Box(modifier = Modifier.padding(vertical = 6.dp)) {
                    TypingDots()
                }
            }

            if (
                message.state == MessageState.STOPPED &&
                message.content.isBlank() &&
                message.reasoning.isBlank()
            ) {
                Text(
                    text = "已停止",
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }

            if (message.state == MessageState.ERROR) {
                Column(modifier = Modifier.padding(top = 6.dp)) {
                    Text(
                        text = message.error ?: "当前服务异常，请稍后再试",
                        fontSize = 13.sp,
                        color = colors.danger,
                    )
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clickableNoRipple(onClick = onRegenerate)
                            .background(colors.elevated, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DsIcon(DsIconType.Regenerate, colors.textPrimary, size = 14.dp, strokeWidth = 1.6.dp)
                        Spacer(Modifier.width(6.dp))
                        Text(text = "重试", fontSize = 13.sp, color = colors.textPrimary)
                    }
                }
            }

            if (message.state != MessageState.STREAMING && message.content.isNotBlank()) {
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SmallAction(DsIconType.Copy, colors.textSecondary) {
                        copyToClipboard(context, "消息", message.content)
                        Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                    }
                    SmallAction(DsIconType.Regenerate, colors.textSecondary, onRegenerate)
                    SmallAction(
                        DsIconType.Like,
                        if (message.feedback == 1) colors.accent else colors.textSecondary,
                    ) { onFeedback(1) }
                    SmallAction(
                        DsIconType.Dislike,
                        if (message.feedback == -1) colors.accent else colors.textSecondary,
                    ) { onFeedback(-1) }
                    SmallAction(DsIconType.Speaker, colors.textSecondary, onReadAloud)
                }
            }
        }
    }
}

@Composable
private fun SmallAction(
    type: DsIconType,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clickableNoRipple(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        DsIcon(type = type, tint = tint, size = 17.dp, strokeWidth = 1.6.dp)
    }
}

@Composable
private fun ToolRow(text: String, animated: Boolean = false) {
    val colors = DsTheme.colors
    Row(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .background(colors.elevated, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DsIcon(DsIconType.Globe, colors.textSecondary, size = 14.dp, strokeWidth = 1.5.dp)
        Spacer(Modifier.width(6.dp))
        Text(text = text, fontSize = 12.sp, color = colors.textSecondary)
        if (animated) {
            Spacer(Modifier.width(6.dp))
            TypingDots(color = colors.textTertiary)
        }
    }
}

@Composable
private fun ThinkingBlock(
    reasoning: String,
    thinkMillis: Long,
    streaming: Boolean,
    autoFold: Boolean,
    textScale: Float,
) {
    val colors = DsTheme.colors
    var expanded by remember { mutableStateOf(true) }
    LaunchedEffect(streaming) {
        if (!streaming && autoFold) expanded = false
    }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "think-chevron")

    val header = when {
        streaming -> "正在思考"
        thinkMillis > 0 -> "已思考（用时 ${(thinkMillis / 1000f).roundToInt().coerceAtLeast(1)} 秒）"
        else -> "已深度思考"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.elevated, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickableNoRipple { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DsIcon(DsIconType.Bulb, colors.textSecondary, size = 15.dp, strokeWidth = 1.6.dp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = header,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary,
                modifier = Modifier.weight(1f),
            )
            DsIcon(
                type = DsIconType.ChevronDown,
                tint = colors.textSecondary,
                size = 16.dp,
                strokeWidth = 1.6.dp,
                modifier = Modifier.rotate(rotation),
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Text(
                text = reasoning,
                fontSize = (13f * textScale).sp,
                lineHeight = (21f * textScale).sp,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
