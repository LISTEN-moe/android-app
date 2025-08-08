package me.echeung.moemoekyun.service

import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.scopes.ServiceScoped
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.ext.toMediaItem
import javax.inject.Inject

class PlaybackServicePlayerListener @AssistedInject constructor(
    @Assisted private val player: Player,
    private val preferenceUtil: PreferenceUtil
) : Player.Listener {
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
        fun create(player: Player): PlaybackServicePlayerListener
    }
}
