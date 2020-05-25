package me.echeung.moemoekyun.cast

import com.google.android.exoplayer2.ext.cast.CastPlayer
import me.echeung.moemoekyun.client.stream.player.StreamPlayer

class CastStreamPlayer(private val castPlayer: CastPlayer) : StreamPlayer<CastPlayer>() {
    override fun initPlayer() {
        player = castPlayer
    }
}
