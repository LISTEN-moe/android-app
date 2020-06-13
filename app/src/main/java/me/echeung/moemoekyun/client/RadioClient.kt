package me.echeung.moemoekyun.client

import me.echeung.moemoekyun.client.api.APIClient
import me.echeung.moemoekyun.client.api.library.Jpop
import me.echeung.moemoekyun.client.api.library.Kpop
import me.echeung.moemoekyun.client.api.library.Library
import me.echeung.moemoekyun.client.api.socket.Socket
import me.echeung.moemoekyun.client.auth.AuthUtil
import me.echeung.moemoekyun.client.network.NetworkClient
import me.echeung.moemoekyun.client.stream.Stream
import me.echeung.moemoekyun.util.PreferenceUtil

class RadioClient(
    authUtil: AuthUtil,
    networkClient: NetworkClient,
    private val preferenceUtil: PreferenceUtil,
    private val stream: Stream,
    private val socket: Socket
) {

    val api: APIClient

    init {
        setLibrary(preferenceUtil.libraryMode().get())

        this.api = APIClient(networkClient.client, networkClient.apolloCache, authUtil)
    }

    fun changeLibrary(newMode: String) {
        // Avoid unnecessary changes
        if (preferenceUtil.libraryMode().get() == newMode) {
            return
        }

        setLibrary(newMode)

        socket.reconnect()

        // Force it to play with new stream
        if (stream.isPlaying) {
            stream.play()
        }
    }

    private fun setLibrary(libraryName: String) {
        preferenceUtil.libraryMode().set(libraryName)
        library = if (libraryName == Kpop.NAME) Kpop.INSTANCE else Jpop.INSTANCE
    }

    companion object {
        var library: Library = Jpop.INSTANCE
            private set

        fun isKpop(): Boolean {
            return library.name == Kpop.NAME
        }
    }
}
