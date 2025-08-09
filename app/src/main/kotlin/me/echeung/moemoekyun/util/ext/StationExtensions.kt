package me.echeung.moemoekyun.util.ext

import androidx.media3.common.MediaItem
import me.echeung.moemoekyun.client.api.Station

fun Station.toMediaItem() = MediaItem.Builder()
    .setUri(streamUrl)
    .setMediaId(name)
    .build()
