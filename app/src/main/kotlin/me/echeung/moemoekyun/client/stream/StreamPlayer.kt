package me.echeung.moemoekyun.client.stream

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import logcat.logcat
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.ext.isCarUiMode
import me.echeung.moemoekyun.util.system.AudioManagerUtil
import me.echeung.moemoekyun.util.system.NetworkUtil
import javax.inject.Inject

class StreamPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceUtil: PreferenceUtil,
) {

    private var audioManagerUtil: AudioManagerUtil
    private var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener
    private var wasPlayingBeforeLoss: Boolean = false

    private var currentStreamUrl: String? = null
    private var player: ExoPlayer? = null

    private val eventListener = object : Player.Listener {
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

    val isPlaying: Boolean
        get() = player != null && !player!!.isLoading && player!!.isPlaying

    init {
        audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    unduck()
                    if (wasPlayingBeforeLoss) {
                        play()
                    }
                }

                AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    wasPlayingBeforeLoss = isPlaying
                    if (wasPlayingBeforeLoss && (preferenceUtil.shouldPauseAudioOnLoss().get() || context.isCarUiMode())) {
                        pause()
                    }
                }

                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    wasPlayingBeforeLoss = isPlaying
                    if (preferenceUtil.shouldDuckAudio().get()) {
                        duck()
                    }
                }
            }
        }

        audioManagerUtil = AudioManagerUtil(context, audioFocusChangeListener)
    }

    fun initPlayer() {
        if (player == null) {
            player = ExoPlayer.Builder(context).build()

            player!!.setWakeMode(C.WAKE_MODE_NETWORK)

            player!!.addListener(eventListener)
            player!!.volume = 1f

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build()
            player!!.setAudioAttributes(audioAttributes, true)
        }

        // Set stream
        val streamUrl = preferenceUtil.station().get().streamUrl
        if (streamUrl != currentStreamUrl) {
            val dataSourceFactory = DefaultDataSource.Factory(context, DefaultHttpDataSource.Factory().setUserAgent(NetworkUtil.userAgent))
            val streamSource = ProgressiveMediaSource.Factory(dataSourceFactory, DefaultExtractorsFactory())
                .createMediaSource(MediaItem.Builder().setUri(Uri.parse(streamUrl)).build())
            with(player!!) {
                setMediaSource(streamSource)
                prepare()
            }
            currentStreamUrl = streamUrl
        }
    }

    @Synchronized
    fun play() {
        // Request audio focus for playback
        val result = audioManagerUtil.requestAudioFocus()

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            initPlayer()

            if (!isPlaying) {
                player!!.play()
                player!!.seekToDefaultPosition()
            }
        }
    }

    fun pause() {
        player?.pause()
    }

    fun stop() {
        logcat { "Stopping stream" }
        audioManagerUtil.abandonAudioFocus()

        player?.stop()
        player?.clearMediaItems()

        releasePlayer()
    }

    private fun releasePlayer() {
        player?.removeListener(eventListener)
        player?.release()
        player = null
    }

    private fun duck() {
        player?.volume = 0.5f
    }

    private fun unduck() {
        player?.volume = 1f
    }
}
