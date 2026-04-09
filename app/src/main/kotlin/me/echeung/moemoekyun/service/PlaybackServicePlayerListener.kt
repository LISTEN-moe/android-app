package me.echeung.moemoekyun.service

import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.ext.toMediaItem
import me.echeung.moemoekyun.widget.RadioWidgetUpdater

class PlaybackServicePlayerListener @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val player: Player,
    private val preferenceUtil: PreferenceUtil,
    private val radioWidgetUpdater: RadioWidgetUpdater,
    dontBeNoisyReceiverFactory: PlaybackDontBeNoisyReceiver.Factory,
) : Player.Listener {
    private val dontBeNoisyReceiver = dontBeNoisyReceiverFactory.create(player)

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying) {
            val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            context.registerReceiver(dontBeNoisyReceiver, intentFilter)
        } else {
            context.unregisterReceiver(dontBeNoisyReceiver)
        }
        radioWidgetUpdater.onPlayStateChanged(isPlaying)
    }

    override fun onPlayerError(error: PlaybackException) {
        logcat(LogPriority.ERROR) { "An error occurred in the player.\n\n" + error.asLog() }
        val wasPlaying = player.isPlaying

        val fallbackPref = preferenceUtil.useFallbackStream()
        val useFallback = fallbackPref.get() ||
            (error.errorCode in FALLBACK_TRIGGER_ERRORS).also {
                if (it) {
                    logcat { "Decoder error (${error.errorCode}); switching to fallback stream" }
                    fallbackPref.set(true)
                }
            }

        val mediaItem = preferenceUtil.station().get().toMediaItem(useFallback = useFallback)
        player.setMediaItem(mediaItem)
        // Ensure we're "reset" to live
        player.seekToDefaultPosition()
        player.prepare()

        if (wasPlaying) {
            // TODO: avoid infinitely retrying
            player.play()
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(context: Context, player: Player): PlaybackServicePlayerListener
    }
}

private val FALLBACK_TRIGGER_ERRORS = intArrayOf(
    PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
    PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED,
    PlaybackException.ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES,
)
