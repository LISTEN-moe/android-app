package me.echeung.moemoekyun.client.stream.player

import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import kotlin.math.max
import kotlinx.coroutines.delay

abstract class StreamPlayer<T : Player> {

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
        get() = player?.isPlaying ?: false

    abstract fun initPlayer()

    open fun play() {
        initPlayer()

        if (!isPlaying) {
            player!!.play()
            player!!.seekToDefaultPosition()
        }
    }

    fun pause() {
        player?.pause()
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

    private fun releasePlayer() {
        player?.removeListener(eventListener)
        player?.release()
        player = null
    }
}
