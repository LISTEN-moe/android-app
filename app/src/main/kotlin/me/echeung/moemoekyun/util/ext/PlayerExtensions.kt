package me.echeung.moemoekyun.util.ext

import androidx.media3.common.MediaItem
import androidx.media3.common.Player

fun Player.editCurrentMediaItem(block: MediaItem.Builder.(MediaItem?) -> Unit) {
    val mediaItem = currentMediaItem
    val builder = mediaItem?.buildUpon() ?: MediaItem.Builder()
    block(builder, mediaItem)
    replaceMediaItem(0, builder.build())
}
