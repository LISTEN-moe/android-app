package me.echeung.moemoekyun.client.api.v5

import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import me.echeung.moemoekyun.TestSongsQuery
import me.echeung.moemoekyun.client.api.callback.*
import me.echeung.moemoekyun.client.api.v5.library.Library
import me.echeung.moemoekyun.client.auth.AuthUtil
import okhttp3.OkHttpClient

class APIClient(okHttpClient: OkHttpClient, private val authUtil: AuthUtil) {

    private val client: ApolloClient = ApolloClient.builder()
                .serverUrl(Library.API_BASE)
                .okHttpClient(okHttpClient)
                .build()

    fun test() {
        client.query(TestSongsQuery
                .builder()
                .offset(0)
                .count(1)
                .build())
                .enqueue(object : ApolloCall.Callback<TestSongsQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        Log.d("GraphQL test failure", e.message.toString())
                    }

                    override fun onResponse(response: Response<TestSongsQuery.Data>) {
                        Log.d("GraphQL test response", response.data()?.songs().toString())
                    }
                })
    }

    /**
     * Authenticates to the radio.
     *
     * @param username User's username.
     * @param password User's password.
     * @param callback Listener to handle the response.
     */
    fun authenticate(username: String, password: String, callback: LoginCallback) {
    }

    /**
     * Second step for MFA authentication.
     *
     * @param otpToken User's one-time password token.
     * @param callback Listener to handle the response.
     */
    fun authenticateMfa(otpToken: String, callback: LoginCallback) {
    }

    /**
     * Register a new user.
     *
     * @param callback Listener to handle the response.
     */
    fun register(email: String, username: String, password: String, callback: RegisterCallback) {
    }

    /**
     * Gets the user information (id and username).
     *
     * @param callback Listener to handle the response.
     */
    fun getUserInfo(callback: UserInfoCallback) {
    }

    /**
     * Gets a list of all the user's favorited songs.
     *
     * @param callback Listener to handle the response.
     */
    fun getUserFavorites(callback: UserFavoritesCallback) {
    }

    /**
     * Toggles a song's favorite status
     *
     * @param songId     Song to update favorite status of.
     * @param isFavorite Whether the song is currently favorited.
     * @param callback   Listener to handle the response.
     */
    fun toggleFavorite(songId: String, isFavorite: Boolean, callback: FavoriteSongCallback) {
        if (isFavorite) {
            unfavoriteSong(songId, callback)
        } else {
            favoriteSong(songId, callback)
        }
    }

    /**
     * Favorites a song.
     *
     * @param songId   Song to favorite.
     * @param callback Listener to handle the response.
     */
    fun favoriteSong(songId: String, callback: FavoriteSongCallback) {
    }

    /**
     * Unfavorites a song.
     *
     * @param songId   Song to unfavorite.
     * @param callback Listener to handle the response.
     */
    fun unfavoriteSong(songId: String, callback: FavoriteSongCallback) {
    }

    /**
     * Sends a song request to the queue.
     *
     * @param songId   Song to request.
     * @param callback Listener to handle the response.
     */
    fun requestSong(songId: String, callback: RequestSongCallback) {
    }

    /**
     * Gets all songs.
     *
     * @param callback Listener to handle the response.
     */
    fun getSongs(callback: SongsCallback) {
    }

    /**
     * Searches for songs.
     *
     * @param query    Search query string.
     * @param callback Listener to handle the response.
     */
    fun search(query: String?, callback: SearchCallback) {
    }

}
