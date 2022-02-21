package me.echeung.moemoekyun.client

import me.echeung.moemoekyun.client.api.APIClient
import me.echeung.moemoekyun.client.api.Library
import me.echeung.moemoekyun.client.api.socket.Socket
import me.echeung.moemoekyun.client.api.auth.AuthUtil
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

        this.api = APIClient(networkClient.apolloClient, authUtil)
    }

    fun changeLibrary(newLibrary: Library) {
        // Avoid unnecessary changes
        if (preferenceUtil.libraryMode().get() == newLibrary) {
            return
        }

        setLibrary(newLibrary)

        socket.reconnect()

        // Force it to play with new stream
        if (stream.isPlaying) {
            stream.play()
        }
    }

    private fun setLibrary(newLibrary: Library) {
        preferenceUtil.libraryMode().set(newLibrary)
        library = newLibrary
    }

    companion object {
        var library: Library = Library.jpop
            private set

        fun isKpop() = library == Library.kpop
    }
}
