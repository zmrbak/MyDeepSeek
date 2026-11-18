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

val LightDsColors = DsColors(
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    elevated = Color(0xFFF6F7F9),
    bubble = Color(0xFFF1F3F5),
    textPrimary = Color(0xFF1B1C1F),
    textSecondary = Color(0xFF8A8F99),
    textTertiary = Color(0xFFB0B4BC),
    divider = Color(0xFFEDEEF1),
    accent = BrandBlue,
    accentSoft = Color(0xFFEDF0FF),
    accentOn = Color(0xFFFFFFFF),
    codeBackground = Color(0xFFF6F7F9),
    codeHeader = Color(0xFFEFF1F4),
    codeText = Color(0xFF24292F),
    danger = DangerRed,
)

val DarkDsColors = DsColors(
    background = Color(0xFF17181A),
    surface = Color(0xFF1D1E21),
    elevated = Color(0xFF26272B),
    bubble = Color(0xFF26272B),
    textPrimary = Color(0xFFE9EAEC),
    textSecondary = Color(0xFF9AA0A6),
    textTertiary = Color(0xFF6E747C),
    divider = Color(0xFF2E3034),
    accent = BrandBlue,
    accentSoft = Color(0xFF252B47),
    accentOn = Color(0xFFFFFFFF),
    codeBackground = Color(0xFF22242A),
    codeHeader = Color(0xFF2A2D34),
    codeText = Color(0xFFE9EAEC),
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
            primary = BrandBlue,
            onPrimary = Color.White,
            secondary = BrandBlue,
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
            primary = BrandBlue,
            onPrimary = Color.White,
            secondary = BrandBlue,
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
