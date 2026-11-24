package com.example.cardpocket.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = GreenDeep,
    onPrimary = Color.White,
    primaryContainer = GreenDeep,
    onPrimaryContainer = Color.White,
    secondary = Terracotta,
    onSecondary = Color.White,
    background = Bg,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = BadgeBg,
    onSurfaceVariant = TextMuted,
    outline = ChipBorder,
    outlineVariant = DividerColor,
    error = Color(0xFFB3261E),
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9FD3B3),
    onPrimary = Color(0xFF10281B),
    secondary = Color(0xFFE6A98F),
    onSecondary = Color(0xFF3A1A0F),
    background = Color(0xFF14150F),
    onBackground = Color(0xFFEDEBE4),
    surface = Color(0xFF1B1C16),
    onSurface = Color(0xFFEDEBE4),
    surfaceVariant = Color(0xFF2A2B24),
    onSurfaceVariant = Color(0xFFB8B6AE),
    outline = Color(0xFF3E3F36),
    outlineVariant = Color(0xFF2E2F27),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410)
)

@Composable
fun CardPocketTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
