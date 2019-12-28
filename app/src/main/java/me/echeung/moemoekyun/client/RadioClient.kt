package me.echeung.moemoekyun.client

import android.content.Context
import me.echeung.moemoekyun.client.api.APIClient
import me.echeung.moemoekyun.client.api.library.Jpop
import me.echeung.moemoekyun.client.api.library.Kpop
import me.echeung.moemoekyun.client.api.library.Library
import me.echeung.moemoekyun.client.auth.AuthUtil
import me.echeung.moemoekyun.client.cache.ApolloCache
import me.echeung.moemoekyun.client.network.NetworkClient
import me.echeung.moemoekyun.client.socket.Socket
import me.echeung.moemoekyun.client.stream.Stream
import me.echeung.moemoekyun.util.PreferenceUtil

class RadioClient(
        context: Context,
        authUtil: AuthUtil,
        networkClient: NetworkClient,
        apolloCache: ApolloCache,
        private val preferenceUtil: PreferenceUtil
) {

    val api: APIClient
    val socket: Socket
    val stream: Stream

    init {
        setLibrary(preferenceUtil.libraryMode)

        this.api = APIClient(networkClient.client, apolloCache.cache, authUtil)

        this.socket = Socket(context, networkClient.client)
        this.stream = Stream(context)
    }

    fun changeLibrary(newMode: String) {
        setLibrary(newMode)

        preferenceUtil.libraryMode = newMode

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

        fun isKpop(): Boolean {
            return library!!.name == Kpop.NAME
        }
    }
}
