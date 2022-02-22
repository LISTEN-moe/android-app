package me.echeung.moemoekyun.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Color(0xFFFF015B),
    primaryVariant = Color(0xFFFF015B),
    secondary = Color(0xFFC7CCD8),
    background = Color(0xFF1C1D1C),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = DarkColorPalette,
        content = content,
    )
}