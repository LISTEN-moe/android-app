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
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import logcat.logcat
import me.echeung.moemoekyun.service.PlaybackService
import me.echeung.moemoekyun.util.PreferenceUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(UnstableApi::class)
class Stream @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
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

    @Synchronized
    fun play() {
    }

    fun pause() {
        player?.pause()
    }

    fun stop() {
        logcat { "Stopping stream" }
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

}
