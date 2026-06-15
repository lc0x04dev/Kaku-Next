package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NeonDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonMagenta,
    tertiary = NeonLavender,
    background = DeepDark,
    surface = SurfaceDark,
    onBackground = TextWhite,
    onSurface = TextWhite,
    primaryContainer = CardDark,
    onPrimaryContainer = TextWhite,
    secondaryContainer = Color(0xFF2C243C),
    onSecondaryContainer = NeonLavender
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for the modern cyberpunk dark music aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NeonDarkColorScheme,
        typography = Typography,
        content = content
    )
}
