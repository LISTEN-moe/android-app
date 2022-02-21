package me.echeung.moemoekyun.client.stream.player

import android.content.Context
import android.media.AudioManager
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.ext.isAndroidAuto
import me.echeung.moemoekyun.util.system.AudioManagerUtil
import me.echeung.moemoekyun.util.system.NetworkUtil
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LocalStreamPlayer(private val context: Context) : StreamPlayer<ExoPlayer>(), KoinComponent {

    private val preferenceUtil: PreferenceUtil by inject()

    private var wasPlayingBeforeLoss: Boolean = false
    private var audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener {
        when (it) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                unduck()
                if (wasPlayingBeforeLoss) {
                    play()
                }
            }

            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                wasPlayingBeforeLoss = isPlaying
                if (wasPlayingBeforeLoss && (preferenceUtil.shouldPauseAudioOnLoss() || context.isAndroidAuto())) {
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
    private var audioManagerUtil = AudioManagerUtil(context, audioFocusChangeListener)

    override fun initPlayer() {
        if (player == null) {
            player = ExoPlayer.Builder(context)
                .setAudioAttributes(AudioAttributes.Builder()
                    .setContentType(C.CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(), true)
                .setWakeMode(C.WAKE_MODE_NETWORK)
                .build()

            player!!.addListener(eventListener)
            player!!.volume = 1f
        }

        // Set stream
        val streamUrl = RadioClient.library.streamUrl
        if (streamUrl != currentStreamUrl) {
            val dataSourceFactory = DefaultDataSource.Factory(context, DefaultHttpDataSource.Factory().setUserAgent(NetworkUtil.userAgent))
            val streamSource = ProgressiveMediaSource.Factory(dataSourceFactory, DefaultExtractorsFactory())
                .createMediaSource(MediaItem.fromUri(streamUrl))
            with(player!!) {
                setMediaSource(streamSource)
                prepare()
            }
            currentStreamUrl = streamUrl
        }
    }

    @Synchronized
    override fun play() {
        if (audioManagerUtil.requestAudioFocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            super.play()
        }
    }

    override fun stop() {
        audioManagerUtil.abandonAudioFocus()
        super.stop()
    }
}
