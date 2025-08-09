package me.echeung.moemoekyun.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import me.echeung.moemoekyun.util.PreferenceUtil

@OptIn(UnstableApi::class)
class PlaybackDontBeNoisyReceiver @AssistedInject constructor(
    @Assisted private val player: Player,
    private val preferenceUtil: PreferenceUtil,
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            if (preferenceUtil.shouldPauseOnNoisy().get()) {
                player.pause()
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(player: Player): PlaybackDontBeNoisyReceiver
    }
}
