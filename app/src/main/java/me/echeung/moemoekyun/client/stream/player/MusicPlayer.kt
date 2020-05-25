package me.echeung.moemoekyun.client.stream.player

import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import kotlin.math.max
import kotlinx.coroutines.delay

abstract class MusicPlayer<T : Player> {

    protected val eventListener = object : Player.EventListener {
        override fun onPlayerError(error: ExoPlaybackException) {
            // Try to reconnect to the stream
            val wasPlaying = isPlaying

            releasePlayer()
            initPlayer()
            if (wasPlaying) {
                play()
            }
        }
    }

    protected var currentStreamUrl: String? = null

    protected var player: T? = null

    val isStarted: Boolean
        get() = player != null

    val isPlaying: Boolean
        get() = player?.playWhenReady ?: false

    abstract fun initPlayer()

    open fun play() {
        initPlayer()

        if (!isPlaying) {
            player!!.playWhenReady = true
            player!!.seekToDefaultPosition()
        }
    }

    fun pause() {
        player?.playWhenReady = false
    }

    open fun stop() {
        player?.stop(true)

        releasePlayer()
    }

    suspend fun fadeOut() {
        while (player != null) {
            val vol = player!!.audioComponent!!.volume
            val newVol = max(0f, vol - 0.05f)

            if (newVol <= 0) {
                break
            }

            player!!.audioComponent!!.volume = newVol

            delay(200)
        }

        stop()
    }

    fun duck() {
        player?.audioComponent?.volume = 0.5f
    }

    fun unduck() {
        player?.audioComponent?.volume = 1f
    }

    private fun releasePlayer() {
        player?.removeListener(eventListener)
        player?.release()
        player = null
    }
}
