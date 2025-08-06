package me.echeung.moemoekyun.service

import androidx.annotation.OptIn
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import logcat.logcat

@OptIn(UnstableApi::class)
class PlaybackPlayer(val player: Player) : ForwardingPlayer(player) {

    override fun play() {
        logcat { "will seek to default position and start playing" }
        player.seekToDefaultPosition()
        super.play()
    }
}
