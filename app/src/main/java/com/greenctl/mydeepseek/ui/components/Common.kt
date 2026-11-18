package com.greenctl.mydeepseek.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenctl.mydeepseek.ui.theme.DsTheme

@Composable
fun Modifier.clickableNoRipple(enabled: Boolean = true, onClick: () -> Unit): Modifier {
    val source = remember { MutableInteractionSource() }
    return this.clickable(
        interactionSource = source,
        indication = null,
        enabled = enabled,
        onClick = onClick,
    )
}

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

@Composable
fun DsIconButton(
    type: DsIconType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = DsTheme.colors.textSecondary,
    iconSize: Dp = 22.dp,
    enabled: Boolean = true,
    strokeWidth: Dp = 1.7.dp,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickableNoRipple(enabled = enabled, onClick = onClick)
            .size(if (iconSize >= 22.dp) 40.dp else 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        DsIcon(
            type = type,
            tint = if (enabled) tint else tint.copy(alpha = 0.35f),
            size = iconSize,
            strokeWidth = strokeWidth,
        )
    }
}

@Composable
fun AssistantAvatar(size: Dp = 28.dp) {
    val colors = DsTheme.colors
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(colors.accent),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "D",
            color = Color.White,
            fontSize = (size.value * 0.5f).sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun TypingDots(color: Color = DsTheme.colors.textSecondary) {
    val transition = rememberInfiniteTransition(label = "typing")
    val base = transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "typing-alpha",
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .alpha((base.value - index * 0.18f).coerceIn(0.15f, 1f))
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color),
            )
        }
    }
}
