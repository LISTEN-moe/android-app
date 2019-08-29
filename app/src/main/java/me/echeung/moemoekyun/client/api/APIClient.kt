package me.echeung.moemoekyun.client.api

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import me.echeung.moemoekyun.CheckFavoriteQuery
import me.echeung.moemoekyun.FavoriteMutation
import me.echeung.moemoekyun.FavoritesQuery
import me.echeung.moemoekyun.LoginMfaMutation
import me.echeung.moemoekyun.LoginMutation
import me.echeung.moemoekyun.RegisterMutation
import me.echeung.moemoekyun.RequestSongMutation
import me.echeung.moemoekyun.SongsQuery
import me.echeung.moemoekyun.UserQuery
import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.client.api.callback.FavoriteSongCallback
import me.echeung.moemoekyun.client.api.callback.IsFavoriteCallback
import me.echeung.moemoekyun.client.api.callback.LoginCallback
import me.echeung.moemoekyun.client.api.callback.RegisterCallback
import me.echeung.moemoekyun.client.api.callback.RequestSongCallback
import me.echeung.moemoekyun.client.api.callback.SearchCallback
import me.echeung.moemoekyun.client.api.callback.SongsCallback
import me.echeung.moemoekyun.client.api.callback.UserFavoritesCallback
import me.echeung.moemoekyun.client.api.callback.UserInfoCallback
import me.echeung.moemoekyun.client.api.data.transform
import me.echeung.moemoekyun.client.api.library.Library
import me.echeung.moemoekyun.client.auth.AuthTokenUtil
import me.echeung.moemoekyun.client.cache.SongsCache
import me.echeung.moemoekyun.client.model.Song
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class APIClient(okHttpClient: OkHttpClient, private val authTokenUtil: AuthTokenUtil) {

    private val client: ApolloClient
    private val songsCache: SongsCache

    init {
        // Automatically add auth token to requests
        val authClient = okHttpClient.newBuilder()
                .addNetworkInterceptor(object : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                        val original = chain.request()
                        val builder = original.newBuilder().method(original.method, original.body)

                        // MFA login
                        if (authTokenUtil.mfaToken != null) {
                            builder.header("Authorization", authTokenUtil.mfaAuthTokenWithPrefix)
                        }

                        // Authorized calls
                        if (authTokenUtil.isAuthenticated) {
                            builder.header("Authorization", authTokenUtil.authTokenWithPrefix)
                        }

                        return chain.proceed(builder.build())
                    }
                })
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build()

        client = ApolloClient.builder()
            .serverUrl(Library.API_BASE)
            .okHttpClient(authClient)
            .build()

        songsCache = SongsCache(this)
    }

    /**
     * Authenticates to the radio.
     *
     * @param username User's username.
     * @param password User's password.
     * @param callback Listener to handle the response.
     */
    fun authenticate(username: String, password: String, callback: LoginCallback) {
        client.mutate(LoginMutation(username, password))
                .enqueue(object : ApolloCall.Callback<LoginMutation.Data>() {
                    override fun onFailure(e: ApolloException) {
                        callback.onFailure(e.message)
                    }

                    override fun onResponse(response: Response<LoginMutation.Data>) {
                        val userToken = response.data()?.login?.token!!

                        if (response.data()?.login?.mfa!!) {
                            authTokenUtil.mfaToken = userToken
                            callback.onMfaRequired(userToken)
                            return
                        }

                        authTokenUtil.authToken = userToken
                        callback.onSuccess(userToken)
                    }
                })
    }

    /**
     * Second step for MFA authentication.
     *
     * @param otpToken User's one-time password token.
     * @param callback Listener to handle the response.
     */
    fun authenticateMfa(otpToken: String, callback: LoginCallback) {
        client.mutate(LoginMfaMutation(otpToken))
                .enqueue(object : ApolloCall.Callback<LoginMfaMutation.Data>() {
                    override fun onFailure(e: ApolloException) {
                        callback.onFailure(e.message)
                    }

                    override fun onResponse(response: Response<LoginMfaMutation.Data>) {
                        val userToken = response.data()?.loginMFA?.token!!
                        authTokenUtil.authToken = userToken
                        authTokenUtil.clearMfaAuthToken()
                        callback.onSuccess(userToken)
                    }
                })
    }

    /**
     * Register a new user.
     *
     * @param callback Listener to handle the response.
     */
    fun register(email: String, username: String, password: String, callback: RegisterCallback) {
        client.mutate(RegisterMutation(email, username, password))
                .enqueue(object : ApolloCall.Callback<RegisterMutation.Data>() {
                    override fun onFailure(e: ApolloException) {
                        callback.onFailure(e.message)
                    }

                    override fun onResponse(response: Response<RegisterMutation.Data>) {
                        callback.onSuccess()
                    }
                })
    }

    /**
     * Gets the user information (id and username).
     *
     * @param callback Listener to handle the response.
     */
    fun getUserInfo(callback: UserInfoCallback) {
        client.query(UserQuery("@me"))
                .enqueue(object : ApolloCall.Callback<UserQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        callback.onFailure(e.message)
                    }

                    override fun onResponse(response: Response<UserQuery.Data>) {
                        callback.onSuccess(response.data()?.user!!.transform())
                    }
                })
    }

    /**
     * Gets a list of all the user's favorited songs.
     *
     * @param callback Listener to handle the response.
     */
    fun getUserFavorites(callback: UserFavoritesCallback) {
        // TODO: do actual pagination
        client.query(FavoritesQuery("@me", 0, 2500, Input.optional(RadioClient.isKpop())))
                .enqueue(object : ApolloCall.Callback<FavoritesQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        callback.onFailure(e.message)
                    }

                    // TODO: get less info for this and search, and get more on detail opening

                    override fun onResponse(response: Response<FavoritesQuery.Data>) {
                        callback.onSuccess(
                                response.data()?.user?.favorites?.favorites
                                        ?.mapNotNull { it?.song }
                                        ?.map { it.transform() }
                                        ?: emptyList())
                    }
                })
    }

    /**
     * Gets the favorited status of a list of songs.
     *
     * @param songIds IDs of songs to check status of.
     * @param callback Listener to handle the response.
     */
    fun isFavorite(songIds: List<Int>, callback: IsFavoriteCallback) {
        client.query(CheckFavoriteQuery(songIds))
                .enqueue(object : ApolloCall.Callback<CheckFavoriteQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        callback.onFailure(e.message)
                    }

                    override fun onResponse(response: Response<CheckFavoriteQuery.Data>) {
                        callback.onSuccess(
                                response.data()?.checkFavorite?.filterNotNull()
                                        ?: emptyList())
                    }
                })
    }

    /**
     * Toggles a song's favorite status
     *
     * @param songId Song to update favorite status of.
     * @param callback Listener to handle the response.
     */
    fun toggleFavorite(songId: Int, callback: FavoriteSongCallback) {
        client.mutate(FavoriteMutation(songId))
                .enqueue(object : ApolloCall.Callback<FavoriteMutation.Data>() {
                    override fun onFailure(e: ApolloException) {
                        callback.onFailure(e.message)
                    }

                    override fun onResponse(response: Response<FavoriteMutation.Data>) {
                        callback.onSuccess()
                    }
                })
    }

    /**
     * Sends a song request to the queue.
     *
     * @param songId Song to request.
     * @param callback Listener to handle the response.
     */
    fun requestSong(songId: Int, callback: RequestSongCallback) {
        client.mutate(RequestSongMutation(songId, Input.optional(RadioClient.isKpop())))
                .enqueue(object : ApolloCall.Callback<RequestSongMutation.Data>() {
                    override fun onFailure(e: ApolloException) {
                        callback.onFailure(e.message)
                    }

                    override fun onResponse(response: Response<RequestSongMutation.Data>) {
                        callback.onSuccess()
                    }
                })
    }

    /**
     * Searches for songs.
     *
     * @param query Search query string.
     * @param callback Listener to handle the response.
     */
    fun search(query: String?, callback: SearchCallback) {
        songsCache.getSongs(object : SongsCache.Callback {
            override fun onRetrieve(songs: List<Song>?) {
                val filteredSongs = filterSongs(songs!!, query)
                callback.onSuccess(filteredSongs)
            }

            override fun onFailure(message: String?) {
                callback.onFailure(message)
            }
        })
    }

    /**
     * Gets all songs.
     *
     * @param callback Listener to handle the response.
     */
    fun getAllSongs(callback: SongsCallback) {
        // TODO: do actual pagination
        client.query(SongsQuery(0, 50000, Input.optional(RadioClient.isKpop())))
                .enqueue(object : ApolloCall.Callback<SongsQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        callback.onFailure(e.message)
                    }

                    override fun onResponse(response: Response<SongsQuery.Data>) {
                        callback.onSuccess(
                                response.data()?.songs?.songs?.map { it.transform() } ?: emptyList())
                    }
                })
    }

    private fun filterSongs(songs: List<Song>, query: String?): List<Song> {
        return songs.asSequence()
                .filter { song -> song.search(query) }
                .toList()
    }
}
