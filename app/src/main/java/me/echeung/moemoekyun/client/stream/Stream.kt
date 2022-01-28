package me.echeung.moemoekyun.client.stream

import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import me.echeung.moemoekyun.client.stream.player.LocalStreamPlayer
import me.echeung.moemoekyun.client.stream.player.StreamPlayer
import me.echeung.moemoekyun.util.ext.launchIO

class Stream(context: Context) {

    val flow = MutableSharedFlow<State>(replay = 1)

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

        launchIO {
            flow.emit(State.PLAY)
        }
    }

    fun pause() {
        getCurrentPlayer().pause()

        launchIO {
            flow.emit(State.PAUSE)
        }
    }

    fun stop() {
        getCurrentPlayer().stop()

        launchIO {
            flow.emit(State.STOP)
        }
    }

    fun fadeOut() {
        launchIO {
            getCurrentPlayer().fadeOut()

            flow.emit(State.STOP)
        }
    }

    enum class State {
        PLAY,
        PAUSE,
        STOP,
    }
}
