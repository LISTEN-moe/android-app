package me.echeung.moemoekyun.client.stream.player

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.ext.isCarUiMode
import me.echeung.moemoekyun.util.system.AudioManagerUtil
import me.echeung.moemoekyun.util.system.NetworkUtil
import org.koin.core.KoinComponent
import org.koin.core.inject

class LocalStreamPlayer(private val context: Context) : StreamPlayer<SimpleExoPlayer>(), KoinComponent {

    private val preferenceUtil: PreferenceUtil by inject()

    private var audioManagerUtil: AudioManagerUtil
    private var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener
    private var wasPlayingBeforeLoss: Boolean = false

    private val focusLock = Any()

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
                    if (wasPlayingBeforeLoss && (preferenceUtil.shouldPauseAudioOnLoss() || context.isCarUiMode())) {
                        pause()
                    }
                }

                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    wasPlayingBeforeLoss = isPlaying
                    if (preferenceUtil.shouldDuckAudio()) {
                        duck()
                    }
                }
            }
        }

        audioManagerUtil = AudioManagerUtil(context, audioFocusChangeListener)
    }

    override fun initPlayer() {
        if (player == null) {
            player = SimpleExoPlayer.Builder(context).build()

            player!!.setWakeMode(C.WAKE_MODE_NETWORK)

            player!!.addListener(eventListener)
            player!!.volume = 1f

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build()
            player!!.audioAttributes = audioAttributes
        }

        // Set stream
        val streamUrl = RadioClient.library.streamUrl
        if (streamUrl != currentStreamUrl) {
            val dataSourceFactory = DefaultDataSourceFactory(context, NetworkUtil.userAgent)
            val streamSource = ProgressiveMediaSource.Factory(dataSourceFactory, DefaultExtractorsFactory())
                .createMediaSource(Uri.parse(streamUrl))

            player!!.prepare(streamSource)
            currentStreamUrl = streamUrl
        }
    }

    override fun play() {
        // Request audio focus for playback
        val result = audioManagerUtil.requestAudioFocus()

        synchronized(focusLock) {
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                super.play()
            }
        }
    }

    override fun stop() {
        audioManagerUtil.abandonAudioFocus()

        super.stop()
    }

    private fun duck() {
        player?.audioComponent?.volume = 0.5f
    }

    private fun unduck() {
        player?.audioComponent?.volume = 1f
    }
}
