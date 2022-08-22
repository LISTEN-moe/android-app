package me.echeung.moemoekyun.client.stream

import android.content.Context
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import me.echeung.moemoekyun.client.stream.player.LocalStreamPlayer
import me.echeung.moemoekyun.util.ext.launchIO
import me.echeung.moemoekyun.util.ext.launchNow

class Stream(context: Context) {

    val channel = ConflatedBroadcastChannel<State>()

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

        launchNow {
            channel.send(State.PLAY)
        }
    }

    fun pause() {
        player.pause()

        launchNow {
            channel.send(State.PAUSE)
        }
    }

    fun stop() {
        player.stop()

        launchNow {
            channel.send(State.STOP)
        }
    }

    fun fadeOut() {
        launchIO {
            player.fadeOut()

            channel.send(State.STOP)
        }
    }

    enum class State {
        PLAY,
        PAUSE,
        STOP,
    }
}
