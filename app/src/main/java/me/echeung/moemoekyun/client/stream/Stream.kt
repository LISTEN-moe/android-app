package me.echeung.moemoekyun.client.stream

import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import me.echeung.moemoekyun.client.stream.player.LocalStreamPlayer
import me.echeung.moemoekyun.client.stream.player.StreamPlayer
import me.echeung.moemoekyun.util.ext.launchIO
import me.echeung.moemoekyun.util.ext.launchNow

@OptIn(ExperimentalCoroutinesApi::class)
class Stream(context: Context) {

    val channel = ConflatedBroadcastChannel<State>()

    private var localPlayer: StreamPlayer<*> = LocalStreamPlayer(context)
    private var altPlayer: StreamPlayer<*>? = null

    val isStarted: Boolean
        get() = getCurrentPlayer().isStarted

    val isPlaying: Boolean
        get() = getCurrentPlayer().isPlaying

    /**
     * Used to "replace" the local player with a Cast player.
     */
    fun setAltPlayer(player: StreamPlayer<*>?) {
        val wasPlaying = isPlaying
        getCurrentPlayer().pause()

        altPlayer = player

        if (wasPlaying || altPlayer != null) {
            getCurrentPlayer().play()
        }
    }

    private fun getCurrentPlayer(): StreamPlayer<*> {
        return altPlayer ?: localPlayer
    }

    fun toggle() {
        if (isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        getCurrentPlayer().play()

        launchNow {
            channel.send(State.PLAY)
        }
    }

    fun pause() {
        getCurrentPlayer().pause()

        launchNow {
            channel.send(State.PAUSE)
        }
    }

    fun stop() {
        getCurrentPlayer().stop()

        launchNow {
            channel.send(State.STOP)
        }
    }

    fun fadeOut() {
        launchIO {
            getCurrentPlayer().fadeOut()

            channel.send(State.STOP)
        }
    }

    enum class State {
        PLAY,
        PAUSE,
        STOP,
    }
}
