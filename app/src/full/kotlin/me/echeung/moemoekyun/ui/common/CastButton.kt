package me.echeung.moemoekyun.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.media3.cast.MediaRouteButton
import me.echeung.moemoekyun.ui.util.rememberIsWifiConnected

@Composable
fun CastButton() {
    val isWifiConnected by rememberIsWifiConnected()

    if (isWifiConnected) {
        MediaRouteButton()
    }
}
