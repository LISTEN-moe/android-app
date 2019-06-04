package me.echeung.moemoekyun.client

import android.content.Context
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.client.api.APIClient
import me.echeung.moemoekyun.client.api.BridgeAPIClient
import me.echeung.moemoekyun.client.api.library.Jpop
import me.echeung.moemoekyun.client.api.library.Kpop
import me.echeung.moemoekyun.client.api.library.Library
import me.echeung.moemoekyun.client.auth.AuthUtil
import me.echeung.moemoekyun.client.network.NetworkClient
import me.echeung.moemoekyun.client.socket.Socket
import me.echeung.moemoekyun.client.stream.Stream

class RadioClient(context: Context) {

    val api: APIClient
    val socket: Socket
    val stream: Stream
    val authUtil: AuthUtil

    init {
        setLibrary(App.preferenceUtil!!.libraryMode)

        val okHttpClient = NetworkClient.client

        this.authUtil = AuthUtil(context)

        this.api = BridgeAPIClient(okHttpClient, authUtil)

        this.socket = Socket(okHttpClient, authUtil)
        this.stream = Stream(context)
    }

    fun changeLibrary(newMode: String) {
        setLibrary(newMode)

        socket.reconnect()

        // Force it to play with new stream
        if (stream.isPlaying) {
            stream.play()
        }
    }

    private fun setLibrary(libraryName: String) {
        library = if (libraryName == Kpop.NAME) Kpop.INSTANCE else Jpop.INSTANCE
    }

    companion object {
        var library: Library? = null
            private set
    }
}
