package me.echeung.moemoekyun.client.api

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.extractor.DefaultExtractorsFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import logcat.logcat
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.ext.isCarUiMode
import me.echeung.moemoekyun.util.system.AudioManagerUtil
import me.echeung.moemoekyun.util.system.NetworkUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Stream @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceUtil: PreferenceUtil,
) {

    private val _flow = MutableStateFlow(State.STOPPED)
    val flow = _flow.asStateFlow()

    private var audioManagerUtil: AudioManagerUtil
    private var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener
    private var wasPlayingBeforeLoss: Boolean = false

    private var currentStreamUrl: String? = null
    private var player: ExoPlayer? = null

    private val eventListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            logcat { "stream onPlaybackStateChanged: $playbackState" }
            if (playbackState == Player.STATE_BUFFERING) {
                _flow.value = State.BUFFERING
            } else if (playbackState == Player.STATE_READY) {
                if (isPlaying) {
                    _flow.value = State.PLAYING
                } else {
                    play()
                }
            } else if (player?.playWhenReady == true) {
                _flow.value = State.PAUSED
            }
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

    val isPlaying: Boolean
        get() = player?.isPlaying ?: false

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

    @Synchronized
    fun play() {
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
        _flow.value = State.PAUSED
    }

    fun stop() {
        logcat { "Stopping stream" }
        audioManagerUtil.abandonAudioFocus()

        player?.stop()
        player?.clearMediaItems()

        releasePlayer()
        _flow.value = State.STOPPED
    }

    // TODO: hook up to MediaSession directly
    @androidx.annotation.OptIn(UnstableApi::class)
    private fun initPlayer() {
        if (player == null) {
            player = ExoPlayer.Builder(context)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .setUsage(C.USAGE_MEDIA)
                        .build(),
                    true,
                )
                .setWakeMode(C.WAKE_MODE_NETWORK)
                .build()
                .also {
                    it.addListener(eventListener)
                    it.volume = 1f
                }
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

    enum class State {
        PLAYING,
        PAUSED,
        STOPPED,
        BUFFERING,
    }
}
