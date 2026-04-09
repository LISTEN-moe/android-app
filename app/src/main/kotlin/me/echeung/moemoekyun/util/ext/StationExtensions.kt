package me.echeung.moemoekyun.util.ext

import androidx.media3.common.MediaItem
import me.echeung.moemoekyun.client.api.Station

fun Station.toMediaItem(useFallback: Boolean = false) = MediaItem.Builder()
    .setUri(if (useFallback) fallbackStreamUrl else streamUrl)
    .setMediaId(name)
    .build()
