package com.greenctl.mydeepseek.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 应用语义色板，除 Material 角色色之外，额外提供聊天气泡、代码块等专用颜色。
 */
@Immutable
data class DsColors(
    val background: Color,
    val surface: Color,
    val elevated: Color,
    val bubble: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val divider: Color,
    val accent: Color,
    val accentSoft: Color,
    val accentOn: Color,
    val codeBackground: Color,
    val codeHeader: Color,
    val codeText: Color,
    val danger: Color,
)

/**
 * 浅色：#FFFFFF 底 + #426EFE 主色，取自安装包 color/primary 与 splashscreenBackground。
 */
val LightDsColors = DsColors(
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    elevated = Color(0xFFF5F6F7),
    bubble = Color(0xFFF2F3F5),
    textPrimary = Color(0xFF121417),
    textSecondary = Color(0xFF8B9099),
    textTertiary = Color(0xFFB4B8BE),
    divider = Color(0xFFECECEE),
    accent = BrandBlue,
    accentSoft = Color(0xFFEDF1FF),
    accentOn = Color(0xFFFFFFFF),
    codeBackground = Color(0xFFF7F8F9),
    codeHeader = Color(0xFFEFF1F3),
    codeText = Color(0xFF1F2328),
    danger = DangerRed,
)

/**
 * 深色：底色 #0F0F0F、主色 #507BF2、卡片 #333333，均取自安装包资源。
 */
val DarkDsColors = DsColors(
    background = Color(0xFF0F0F0F),
    surface = Color(0xFF171717),
    elevated = Color(0xFF262626),
    bubble = Color(0xFF262626),
    textPrimary = Color(0xFFECECEC),
    textSecondary = Color(0xFF9B9B9B),
    textTertiary = Color(0xFF6E6E6E),
    divider = Color(0xFF2C2C2C),
    accent = BrandBlueDark,
    accentSoft = Color(0xFF1D2740),
    accentOn = Color(0xFFFFFFFF),
    codeBackground = Color(0xFF1B1B1B),
    codeHeader = Color(0xFF262626),
    codeText = Color(0xFFE7E7E7),
    danger = DangerRed,
)

val LocalDsColors = staticCompositionLocalOf { LightDsColors }

object DsTheme {
    val colors: DsColors
        @Composable get() = LocalDsColors.current
}

/**
 * 外观模式：0 跟随系统，1 浅色，2 深色。
 */
@Composable
fun isDarkThemeEnabled(appearance: Int): Boolean = when (appearance) {
    1 -> false
    2 -> true
    else -> isSystemInDarkTheme()
}

@Composable
fun MyDeepSeekTheme(
    appearance: Int = 0,
    content: @Composable () -> Unit,
) {
    val dark = isDarkThemeEnabled(appearance)
    val colors = if (dark) DarkDsColors else LightDsColors
    val scheme = if (dark) {
        darkColorScheme(
            primary = colors.accent,
            onPrimary = Color.White,
            secondary = colors.accent,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.elevated,
            onSurfaceVariant = colors.textSecondary,
            outline = colors.divider,
            error = DangerRed,
        )
    } else {
        lightColorScheme(
            primary = colors.accent,
            onPrimary = Color.White,
            secondary = colors.accent,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.elevated,
            onSurfaceVariant = colors.textSecondary,
            outline = colors.divider,
            error = DangerRed,
        )
    }
    CompositionLocalProvider(LocalDsColors provides colors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = Typography,
            content = content,
        )
    }
}
