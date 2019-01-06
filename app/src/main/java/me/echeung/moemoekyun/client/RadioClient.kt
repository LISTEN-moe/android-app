package me.echeung.moemoekyun.client

import android.content.Context
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.client.api.v4.APIClient
import me.echeung.moemoekyun.client.api.v4.library.Jpop
import me.echeung.moemoekyun.client.api.v4.library.Kpop
import me.echeung.moemoekyun.client.api.v4.library.Library
import me.echeung.moemoekyun.client.auth.AuthUtil
import me.echeung.moemoekyun.client.network.NetworkClient
import me.echeung.moemoekyun.client.socket.Socket
import me.echeung.moemoekyun.client.stream.Stream
import me.echeung.moemoekyun.client.api.v5.APIClient as GraphQLAPIClient

class RadioClient(context: Context) {

    val api: APIClient
    val graphQlApi: GraphQLAPIClient
    val socket: Socket
    val stream: Stream
    val authUtil: AuthUtil

    init {
        setLibrary(App.preferenceUtil!!.libraryMode)

        val okHttpClient = NetworkClient.client

        this.authUtil = AuthUtil(context)

        this.api = APIClient(okHttpClient, authUtil)
        this.graphQlApi = GraphQLAPIClient(okHttpClient, authUtil)

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
        RadioClient.library = if (libraryName == Kpop.NAME) Kpop.INSTANCE else Jpop.INSTANCE
    }

    companion object {
        var library: Library? = null
            private set
    }
}
