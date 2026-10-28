package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CS2ColorScheme = darkColorScheme(
    primary = CS2Gold,
    onPrimary = Color.Black,
    primaryContainer = CS2CardBg,
    onPrimaryContainer = CS2Gold,
    secondary = CS2Cyan,
    onSecondary = Color.Black,
    background = CS2Background,
    onBackground = CS2TextPrimary,
    surface = CS2Surface,
    onSurface = CS2TextPrimary,
    surfaceVariant = CS2CardBg,
    onSurfaceVariant = CS2TextSecondary,
    outline = CS2Border,
    error = CS2Red
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CS2ColorScheme,
        typography = Typography,
        content = content
    )
}
