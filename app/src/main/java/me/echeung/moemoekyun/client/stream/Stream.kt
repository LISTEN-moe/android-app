package me.echeung.moemoekyun.client.stream

import android.content.Context
import android.net.Uri
import android.os.Handler
import com.google.android.exoplayer2.C.CONTENT_TYPE_MUSIC
import com.google.android.exoplayer2.C.USAGE_MEDIA
import com.google.android.exoplayer2.C.WAKE_MODE_NETWORK
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.util.system.NetworkUtil

class Stream(private val context: Context) {

    private val eventListener = object : Player.EventListener {
        override fun onPlayerError(error: ExoPlaybackException) {
            // Try to reconnect to the stream
            val wasPlaying = isPlaying

            releasePlayer()
            init()
            if (wasPlaying) {
                play()
            }
        }
    }

    private var player: SimpleExoPlayer? = null

    private var currentStreamUrl: String? = null

    private var listener: Listener? = null

    val isStarted: Boolean
        get() = player != null

    val isPlaying: Boolean
        get() = player != null && player!!.playWhenReady

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    fun removeListener() {
        this.listener = null
    }

    fun play() {
        init()

        if (!isPlaying) {
            player!!.playWhenReady = true
            player!!.seekToDefaultPosition()
        }

        listener?.onStreamPlay()
    }

    fun pause() {
        if (player != null) {
            player!!.playWhenReady = false
        }

        listener?.onStreamPause()
    }

    fun stop() {
        if (player != null) {
            player!!.stop(true)

            releasePlayer()
        }

        listener?.onStreamStop()
    }

    fun fadeOut() {
        val handler = Handler()
        val runnable = object : Runnable {
            override fun run() {
                if (player == null) {
                    stop()
                    if (listener != null) {
                        listener!!.onStreamStop()
                    }
                    return
                }

                val vol = player!!.volume
                val newVol = vol - 0.05f
                if (newVol <= 0) {
                    stop()
                    if (listener != null) {
                        listener!!.onStreamStop()
                    }
                    return
                }

                player!!.volume = newVol

                handler.postDelayed(this, 200)
            }
        }

        handler.post(runnable)
    }

    fun duck() {
        player?.volume = 0.5f
    }

    fun unduck() {
        player?.volume = 1f
    }

    private fun init() {
        // Create ExoPlayer instance
        if (player == null) {
            player = SimpleExoPlayer.Builder(context).build()

            player!!.setWakeMode(WAKE_MODE_NETWORK)

            player!!.addListener(eventListener)
            player!!.volume = 1f

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(CONTENT_TYPE_MUSIC)
                .setUsage(USAGE_MEDIA)
                .build()
            player!!.audioAttributes = audioAttributes
        }

        // Set stream
        val streamUrl = RadioClient.library!!.streamUrl
        if (streamUrl != currentStreamUrl) {
            val dataSourceFactory = DefaultDataSourceFactory(context, NetworkUtil.userAgent)
            val streamSource = ProgressiveMediaSource.Factory(dataSourceFactory, DefaultExtractorsFactory())
                .createMediaSource(Uri.parse(streamUrl))

            player!!.prepare(streamSource)
            currentStreamUrl = streamUrl
        }
    }

    private fun releasePlayer() {
        if (player != null) {
            player!!.removeListener(eventListener)
            player!!.release()
            player = null
            currentStreamUrl = null
        }
    }

    interface Listener {
        fun onStreamPlay()
        fun onStreamPause()
        fun onStreamStop()
    }
}
