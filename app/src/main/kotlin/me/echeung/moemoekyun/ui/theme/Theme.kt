package me.echeung.moemoekyun.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ThemePalette = darkColorScheme(
    primary = Color(0xFFFF015B),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFFB4B8C2),
    primaryContainer = Color(0xFFFF015B),
    background = Color(0xFF1C1D1C),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ThemePalette,
        content = content,
    )
}
