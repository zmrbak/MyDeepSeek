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
    val accentBorder: Color,
    val accentOn: Color,
    val codeBackground: Color,
    val codeHeader: Color,
    val codeText: Color,
    val danger: Color,
)

/**
 * 浅色：官方设计令牌（neutral-bluish / deepseek 色阶）。
 * 气泡 #EDF3FE、正文 #0F1115、主色 #426EFE 均由官方截图与安装包资源采样得到。
 */
val LightDsColors = DsColors(
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    elevated = Color(0xFFF5F6F7),
    bubble = Color(0xFFEDF3FE),
    textPrimary = Color(0xFF0F1115),
    textSecondary = Color(0xFF61666B),
    textTertiary = Color(0xFFA2A4A6),
    divider = Color(0xFFEBEEF2),
    accent = BrandBlue,
    accentSoft = Color(0xFFEDF3FE),
    accentBorder = Color(0xFFD3E2FF),
    accentOn = Color(0xFFFFFFFF),
    codeBackground = Color(0xFFF9FAFB),
    codeHeader = Color(0xFFF5F6F7),
    codeText = Color(0xFF0F1115),
    danger = DangerRed,
)

/**
 * 深色：底色 #0F0F0F、主色 #507BF2（安装包资源），层色取自官方深色令牌。
 */
val DarkDsColors = DsColors(
    background = Color(0xFF0F0F0F),
    surface = Color(0xFF1B1B1C),
    elevated = Color(0xFF2C2C2E),
    bubble = Color(0xFF283142),
    textPrimary = Color(0xFFF9FAFB),
    textSecondary = Color(0xFFCFD3D6),
    textTertiary = Color(0xFFADB2B8),
    divider = Color(0xFF2A2A2B),
    accent = BrandBlueDark,
    accentSoft = Color(0xFF283142),
    accentBorder = Color(0xFF34415B),
    accentOn = Color(0xFFFFFFFF),
    codeBackground = Color(0xFF1B1B1C),
    codeHeader = Color(0xFF2C2C2E),
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
