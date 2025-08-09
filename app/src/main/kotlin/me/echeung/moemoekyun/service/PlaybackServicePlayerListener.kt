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

class PlaybackServicePlayerListener @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val player: Player,
    private val preferenceUtil: PreferenceUtil,
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
    }
    override fun onPlayerError(error: PlaybackException) {
        logcat(LogPriority.ERROR) { "An error ocurred in the player.\n\n" + error.asLog() }
        val wasPlaying = player.isPlaying

        val mediaItem = preferenceUtil.station().get().toMediaItem()
        player.setMediaItem(mediaItem)
        player.prepare()
        if (wasPlaying) {
            player.play()
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(context: Context, player: Player): PlaybackServicePlayerListener
    }
}
