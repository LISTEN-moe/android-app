package me.echeung.moemoekyun.client.stream

import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import me.echeung.moemoekyun.client.stream.player.LocalPlayer
import me.echeung.moemoekyun.client.stream.player.MusicPlayer
import me.echeung.moemoekyun.util.ext.launchIO
import me.echeung.moemoekyun.util.ext.launchNow

@OptIn(ExperimentalCoroutinesApi::class)
class Stream(context: Context) {

    val channel = ConflatedBroadcastChannel<State>()

    private var player: MusicPlayer<*> = LocalPlayer(context)

    val isStarted: Boolean
        get() = player.isStarted

    val isPlaying: Boolean
        get() = player.isPlaying

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

    fun duck() {
        player.duck()
    }

    fun unduck() {
        player.unduck()
    }

    enum class State {
        PLAY,
        PAUSE,
        STOP,
    }
}
