package me.echeung.moemoekyun.client.stream

import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import me.echeung.moemoekyun.client.stream.player.LocalStreamPlayer
import me.echeung.moemoekyun.client.stream.player.StreamPlayer
import me.echeung.moemoekyun.util.ext.launchIO

class Stream(context: Context) {

    val flow = MutableSharedFlow<State>(replay = 1)

    private var player: StreamPlayer<*> = LocalStreamPlayer(context)

    val isStarted: Boolean
        get() = player.isStarted

    val isPlaying: Boolean
        get() = player.isPlaying


    fun toggle() {
        if (isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        player.play()

        launchIO {
            flow.emit(State.PLAY)
        }
    }

    fun pause() {
        player.pause()

        launchIO {
            flow.emit(State.PAUSE)
        }
    }

    fun stop() {
        player.stop()

        launchIO {
            flow.emit(State.STOP)
        }
    }

    fun fadeOut() {
        launchIO {
            player.fadeOut()

            flow.emit(State.STOP)
        }
    }

    enum class State {
        PLAY,
        PAUSE,
        STOP,
    }
}
