package me.echeung.moemoekyun.service

import androidx.annotation.OptIn
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import logcat.logcat
import javax.inject.Inject

@OptIn(UnstableApi::class)
class PlaybackPlayer @Inject constructor(val player: ExoPlayer) : ForwardingPlayer(player) {

    override fun play() {
        logcat { "will seek to default position and start playing" }
        player.seekToDefaultPosition()
        super.play()
    }
}
