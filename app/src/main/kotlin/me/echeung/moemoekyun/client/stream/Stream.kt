package me.echeung.moemoekyun.client.stream

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Stream @Inject constructor(
    private val player: StreamPlayer,
) {

    private val _flow = MutableStateFlow(State.STOP)
    val flow = _flow.asStateFlow()

    val isPlaying: Boolean
        get() = player.isPlaying

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

    enum class State {
        PLAY,
        PAUSE,
        STOP,
    }
}
