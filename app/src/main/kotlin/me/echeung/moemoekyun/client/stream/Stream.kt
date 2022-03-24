package me.echeung.moemoekyun.client.stream

import android.content.Context
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import me.echeung.moemoekyun.client.stream.player.LocalStreamPlayer
import me.echeung.moemoekyun.client.stream.player.StreamPlayer
import me.echeung.moemoekyun.util.ext.launchIO

class Stream(context: Context) {

    val state = MutableSharedFlow<State>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private var player: StreamPlayer<*> = LocalStreamPlayer(context)

    val isStarted: Boolean
        get() = player.isStarted

    val isPlaying: Boolean
        get() = player.isPlaying

    val isLoading: Boolean
        get() = player.isLoading

    fun toggle() {
        if (player.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        player.play()

        launchIO {
            state.emit(State.Playing)
        }
    }

    fun pause() {
        player.pause()

        launchIO {
            state.emit(State.Paused)
        }
    }

    fun stop() {
        player.stop()

        launchIO {
            state.emit(State.Stopped)
        }
    }

    fun fadeOut() {
        launchIO {
            player.fadeOut()

            state.emit(State.Stopped)
        }
    }

    sealed class State {
        object Loading : State()
        object Playing : State()
        object Paused : State()
        object Stopped : State()
    }
}
