package me.echeung.moemoekyun.client.stream

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class Stream(private val player: StreamPlayer) {

    private val _flow = MutableStateFlow(State.STOP)
    val flow = _flow.asStateFlow()

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

        _flow.value = State.PLAY
    }

    fun pause() {
        player.pause()

        _flow.value = State.PAUSE
    }

    fun stop() {
        player.stop()

        _flow.value = State.STOP
    }

    suspend fun fadeOut() {
        player.fadeOut()

        _flow.value = State.STOP
    }

    enum class State {
        PLAY,
        PAUSE,
        STOP,
    }
}
