package me.echeung.moemoekyun.client.stream.player

import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.delay
import kotlin.math.max

abstract class StreamPlayer<T : Player> {

    protected val eventListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            isLoading = playbackState == Player.STATE_BUFFERING
        }

        override fun onPlayerError(error: PlaybackException) {
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

    var isLoading: Boolean = false
        private set

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
        player?.stop()
        player?.clearMediaItems()

        releasePlayer()
    }

    suspend fun fadeOut() {
        while (player != null) {
            val vol = player!!.volume
            val newVol = max(0f, vol - 0.05f)

            if (newVol <= 0) {
                break
            }

            player!!.volume = newVol

            delay(200)
        }

        stop()
    }

    fun duck() {
        player?.volume = 0.5f
    }

    fun unduck() {
        player?.volume = 1f
    }

    private fun releasePlayer() {
        player?.removeListener(eventListener)
        player?.release()
        player = null
    }
}
