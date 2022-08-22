package me.echeung.moemoekyun.client.stream

import android.content.Context
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.echeung.moemoekyun.client.stream.player.LocalStreamPlayer

class Stream(context: Context) {

    private val _flow = MutableStateFlow(State.STOP)
    val flow = _flow.asStateFlow()

    private val scope = MainScope()

    private val player = LocalStreamPlayer(context)

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

        scope.launch {
            _flow.value = State.PLAY
        }
    }

    fun pause() {
        player.pause()

        scope.launch {
            _flow.value = State.PAUSE
        }
    }

    fun stop() {
        player.stop()

        scope.launch {
            _flow.value = State.STOP
        }
    }

    fun fadeOut() {
        scope.launch {
            player.fadeOut()

            _flow.value = State.STOP
        }
    }

    enum class State {
        PLAY,
        PAUSE,
        STOP,
    }
}
