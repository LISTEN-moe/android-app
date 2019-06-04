package me.echeung.moemoekyun.client.stream

import android.content.Context
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Handler
import com.google.android.exoplayer2.C.CONTENT_TYPE_MUSIC
import com.google.android.exoplayer2.C.USAGE_MEDIA
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.util.system.NetworkUtil
import me.echeung.moemoekyun.util.system.wifiManager

class Stream(private val context: Context) {

    private val eventListener = object : Player.EventListener {
        override fun onPlayerError(error: ExoPlaybackException?) {
            // Try to reconnect to the stream
            val wasPlaying = isPlaying

            releasePlayer()
            init()
            if (wasPlaying) {
                play()
            }
        }
    }

    private var wifiLock: WifiManager.WifiLock? = null
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
            acquireWifiLock()

            player!!.playWhenReady = true
            player!!.seekToDefaultPosition()
        }

        listener?.onStreamPlay()
    }

    fun pause() {
        if (player != null) {
            player!!.playWhenReady = false

            releaseWifiLock()
        }

        listener?.onStreamPause()
    }

    fun stop() {
        if (player != null) {
            player!!.stop(true)

            releasePlayer()
            releaseWifiLock()
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
            player = ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector())

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

    private fun acquireWifiLock() {
        if (wifiLock == null) {
            this.wifiLock = context.wifiManager
                    .createWifiLock(WifiManager.WIFI_MODE_FULL, WIFI_LOCK_TAG)
        }

        if (!wifiLock!!.isHeld) {
            wifiLock!!.acquire()
        }
    }

    private fun releaseWifiLock() {
        if (wifiLock != null && wifiLock!!.isHeld) {
            wifiLock!!.release()
        }
    }

    interface Listener {
        fun onStreamPlay()
        fun onStreamPause()
        fun onStreamStop()
    }

    companion object {
        private const val WIFI_LOCK_TAG = "listenmoe_wifi_lock"
    }
}
