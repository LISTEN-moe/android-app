package me.echeung.moemoekyun.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ThemePalette = darkColorScheme(
    primary = Color(0xFFF60052),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFFB4B8C2),
    primaryContainer = Color(0xFFF60052),
    background = Color(0xFF1A1C29),
    surface = Color(0xFF1A1C29),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ThemePalette,
        content = content,
    )
}
