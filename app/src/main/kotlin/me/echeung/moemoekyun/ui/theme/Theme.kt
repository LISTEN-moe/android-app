package me.echeung.moemoekyun.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import me.echeung.moemoekyun.client.api.Station

private val OnPrimary = Color(0xFFF0F0F2)
private val Secondary = Color(0xFF8F92A1)
private val Background = Color(0xFF1A1D28)
private val Surface = Color(0xFF1F232D)
private val Outline = Color(0xFF2C2829)

private val JpopColorScheme = darkColorScheme(
    primary = Color(0xFFFF015B),
    onPrimary = OnPrimary,
    secondary = Secondary,
    primaryContainer = Color(0xFFF60052),
    background = Background,
    surface = Surface,
    surfaceContainer = Surface,
    outline = Outline,
    outlineVariant = Outline,
)

private val KpopColorScheme = darkColorScheme(
    primary = Color(0xFF30A9ED),
    onPrimary = OnPrimary,
    secondary = Secondary,
    primaryContainer = Color(0xFF1587C9),
    background = Background,
    surface = Surface,
    surfaceContainer = Surface,
    outline = Outline,
    outlineVariant = Outline,
)

private fun colorSchemeForStation(station: Station) = when (station) {
    Station.JPOP -> JpopColorScheme
    Station.KPOP -> KpopColorScheme
}

@Composable
fun AppTheme(station: Station = Station.JPOP, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = colorSchemeForStation(station),
        content = content,
    )
}
