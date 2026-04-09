package me.echeung.moemoekyun.util.ext

import androidx.media3.common.MediaItem
import androidx.media3.common.Player

fun Player.editCurrentMediaItem(block: MediaItem.Builder.(MediaItem?) -> Unit) {
    // Avoid a race condition where the radio state flow emits before the stream URL is set,
    // leaving the playlist empty and causing replaceMediaItem(0, ...) to crash.
    if (mediaItemCount == 0) return

    val mediaItem = currentMediaItem
    val builder = mediaItem?.buildUpon() ?: MediaItem.Builder()
    block(builder, mediaItem)
    replaceMediaItem(0, builder.build())
}
