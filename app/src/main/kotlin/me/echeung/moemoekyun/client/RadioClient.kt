package me.echeung.moemoekyun.client

import android.content.Context
import me.echeung.moemoekyun.client.api.APIClient
import me.echeung.moemoekyun.client.api.Library
import me.echeung.moemoekyun.client.api.socket.Socket
import me.echeung.moemoekyun.client.auth.AuthUtil
import me.echeung.moemoekyun.client.network.NetworkClient
import me.echeung.moemoekyun.client.stream.Stream
import me.echeung.moemoekyun.util.PreferenceUtil

class RadioClient(
    context: Context,
    authUtil: AuthUtil,
    networkClient: NetworkClient,
    private val preferenceUtil: PreferenceUtil,
    private val stream: Stream,
    private val socket: Socket
) {

    val api: APIClient

    init {
        setLibrary(Library.fromId(preferenceUtil.libraryMode().get()))

        this.api = APIClient(context, networkClient.client, authUtil)
    }

    fun changeLibrary(newLibrary: Library) {
        // Avoid unnecessary changes
        if (preferenceUtil.libraryMode().get() == newLibrary.id) {
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
        preferenceUtil.libraryMode().set(newLibrary.id)
        library = newLibrary
    }

    companion object {
        var library: Library = Library.Jpop
            private set

        fun isKpop(): Boolean {
            return library.name == Library.Kpop.id
        }
    }
}
