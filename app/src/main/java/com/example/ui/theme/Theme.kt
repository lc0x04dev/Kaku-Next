package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import com.example.viewmodel.MusicViewModel

@Composable
fun MyApplicationTheme(
    viewModel: MusicViewModel? = null,
    content: @Composable () -> Unit
) {
    // If viewModel is not null, collect state. Otherwise use defaults.
    val isAmoled = viewModel?.amoledScreen?.collectAsState()?.value ?: false
    val useCustomColors = viewModel?.useCustomColors?.collectAsState()?.value ?: false
    val selectedColorVal = viewModel?.selectedCustomColor?.collectAsState()?.value ?: 0xFF00FFFF

    val accentColor = if (useCustomColors) Color(selectedColorVal) else NeonCyan
    val bgThemeColor = if (isAmoled) Color.Black else DeepDark
    val surfaceColor = if (isAmoled) Color(0xFF080808) else SurfaceDark
    val cardColor = if (isAmoled) Color(0xFF0C0C0C) else CardDark

    val themeColorScheme = darkColorScheme(
        primary = accentColor,
        secondary = NeonMagenta,
        tertiary = NeonLavender,
        background = bgThemeColor,
        surface = surfaceColor,
        onBackground = TextWhite,
        onSurface = TextWhite,
        primaryContainer = cardColor,
        onPrimaryContainer = TextWhite,
        secondaryContainer = Color(0xFF2C243C),
        onSecondaryContainer = NeonLavender
    )

    MaterialTheme(
        colorScheme = themeColorScheme,
        typography = Typography,
        content = content
    )
}
