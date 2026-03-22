package me.echeung.moemoekyun.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ThemePalette = darkColorScheme(
    primary = Color(0xFFFF015B),
    onPrimary = Color(0xFFF0F0F2),
    secondary = Color(0xFF8F92A1),
    primaryContainer = Color(0xFFF60052),
    background = Color(0xFF1A1D28),
    surface = Color(0xFF1F232D),
    surfaceContainer = Color(0xFF1F232D),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ThemePalette,
        content = content,
    )
}
