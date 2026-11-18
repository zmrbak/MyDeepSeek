package com.greenctl.mydeepseek.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenctl.mydeepseek.data.Attachment
import com.greenctl.mydeepseek.ui.theme.DsTheme

@Composable
fun ChatInputBar(
    draft: String,
    onDraftChange: (String) -> Unit,
    deepThink: Boolean,
    webSearch: Boolean,
    isGenerating: Boolean,
    attachments: List<Attachment>,
    textScale: Float,
    onToggleDeepThink: () -> Unit,
    onToggleWebSearch: () -> Unit,
    onAttach: () -> Unit,
    onRemoveAttachment: (Int) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DsTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        if (attachments.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                attachments.forEachIndexed { index, attachment ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.elevated)
                            .padding(start = 10.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = attachment.name,
                            fontSize = 12.sp,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(140.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clickableNoRipple { onRemoveAttachment(index) },
                            contentAlignment = Alignment.Center,
                        ) {
                            DsIcon(DsIconType.Close, colors.textSecondary, size = 12.dp, strokeWidth = 1.6.dp)
                        }
                    }
                }
            }
        }

        Surface(
            color = colors.surface,
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, colors.divider),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 8.dp)) {
                BasicTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    textStyle = TextStyle(
                        fontSize = (16f * textScale).sp,
                        lineHeight = (23f * textScale).sp,
                        color = colors.textPrimary,
                    ),
                    cursorBrush = SolidColor(colors.accent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 24.dp, max = 160.dp)
                        .padding(end = 6.dp),
                    maxLines = 8,
                    decorationBox = { innerTextField ->
                        Box {
                            if (draft.isEmpty()) {
                                Text(
                                    text = "发消息",
                                    fontSize = (16f * textScale).sp,
                                    color = colors.textTertiary,
                                )
                            }
                            innerTextField()
                        }
                    },
                )

                Spacer(Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TogglePill(
                        type = DsIconType.Bulb,
                        label = "深度思考",
                        active = deepThink,
                        onClick = onToggleDeepThink,
                    )
                    Spacer(Modifier.width(6.dp))
                    TogglePill(
                        type = DsIconType.Globe,
                        label = "智能搜索",
                        active = webSearch,
                        onClick = onToggleWebSearch,
                    )
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickableNoRipple(onClick = onAttach),
                        contentAlignment = Alignment.Center,
                    ) {
                        DsIcon(DsIconType.Attach, colors.textSecondary, size = 19.dp, strokeWidth = 1.6.dp)
                    }
                    Spacer(Modifier.width(6.dp))
                    if (isGenerating) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colors.elevated)
                                .clickableNoRipple(onClick = onStop),
                            contentAlignment = Alignment.Center,
                        ) {
                            DsIcon(DsIconType.Stop, colors.textPrimary, size = 14.dp)
                        }
                    } else {
                        val canSend = draft.isNotBlank()
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (canSend) colors.accent else colors.elevated)
                                .clickableNoRipple(enabled = canSend, onClick = onSend),
                            contentAlignment = Alignment.Center,
                        ) {
                            DsIcon(
                                type = DsIconType.Send,
                                tint = if (canSend) Color.White else colors.textTertiary,
                                size = 17.dp,
                                strokeWidth = 2.dp,
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = "回答由 AI 生成，仅供参考",
            fontSize = 11.sp,
            color = colors.textTertiary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun TogglePill(
    type: DsIconType,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val colors = DsTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (active) colors.accentSoft else colors.elevated)
            .clickableNoRipple(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DsIcon(
            type = type,
            tint = if (active) colors.accent else colors.textSecondary,
            size = 14.dp,
            strokeWidth = 1.6.dp,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (active) colors.accent else colors.textSecondary,
        )
    }
}
