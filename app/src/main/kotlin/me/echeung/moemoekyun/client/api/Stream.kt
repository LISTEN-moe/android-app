package me.echeung.moemoekyun.client.api

import android.content.ComponentName
import android.content.Context
import android.media.AudioManager
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import logcat.logcat
import me.echeung.moemoekyun.service.PlaybackService
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.ext.isCarUiMode
import me.echeung.moemoekyun.util.ext.toMediaItem
import me.echeung.moemoekyun.util.system.AudioManagerUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(UnstableApi::class)
class Stream @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val preferenceUtil: PreferenceUtil,
    audioManagerUtilFactory: AudioManagerUtil.Factory,
) {

    private var audioManagerUtil: AudioManagerUtil
    private var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener
    private var wasPlayingBeforeLoss: Boolean = false

    private var currentStreamUrl: String? = null
    private var player: MediaController? = null

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
                    if (
                        wasPlayingBeforeLoss &&
                        (preferenceUtil.shouldPauseAudioOnLoss().get() || context.isCarUiMode())
                    ) {
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

        audioManagerUtil = audioManagerUtilFactory.create(audioFocusChangeListener)
    }

    @Synchronized
    fun play() {
        val result = audioManagerUtil.requestAudioFocus()
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            initPlayer()

            if (!isPlaying) {
                // player!!.play()
                // player!!.seekToDefaultPosition()
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

    // TODO: hook up to MediaSession directly
    private fun initPlayer() {
        if (player == null) {
            val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))

            // Build the MediaController asynchronously
            val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

            // Set up a listener to handle the controller when it's ready
            controllerFuture.addListener(
                {
                    player = controllerFuture.get().also {
                        it.addListener(eventListener)
                        it.volume = 1f
                    }
                },
                MoreExecutors.directExecutor(),
            )
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

}
