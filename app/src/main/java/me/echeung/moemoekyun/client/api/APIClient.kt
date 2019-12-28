package me.echeung.moemoekyun.client.api

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.ApolloSubscriptionCall
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.cache.http.ApolloHttpCache
import com.apollographql.apollo.cache.http.DiskLruHttpCacheStore
import com.apollographql.apollo.exception.ApolloException
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.CheckFavoriteQuery
import me.echeung.moemoekyun.FavoriteMutation
import me.echeung.moemoekyun.FavoritesQuery
import me.echeung.moemoekyun.LoginMfaMutation
import me.echeung.moemoekyun.LoginMutation
import me.echeung.moemoekyun.QueueQuery
import me.echeung.moemoekyun.QueueSubscription
import me.echeung.moemoekyun.RegisterMutation
import me.echeung.moemoekyun.RequestSongMutation
import me.echeung.moemoekyun.SongQuery
import me.echeung.moemoekyun.SongsQuery
import me.echeung.moemoekyun.UserQuery
import me.echeung.moemoekyun.UserQueueSubscription
import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.client.api.callback.FavoriteSongCallback
import me.echeung.moemoekyun.client.api.callback.IsFavoriteCallback
import me.echeung.moemoekyun.client.api.callback.LoginCallback
import me.echeung.moemoekyun.client.api.callback.QueueCallback
import me.echeung.moemoekyun.client.api.callback.RegisterCallback
import me.echeung.moemoekyun.client.api.callback.RequestSongCallback
import me.echeung.moemoekyun.client.api.callback.SearchCallback
import me.echeung.moemoekyun.client.api.callback.SongCallback
import me.echeung.moemoekyun.client.api.callback.SongsCallback
import me.echeung.moemoekyun.client.api.callback.UserFavoritesCallback
import me.echeung.moemoekyun.client.api.callback.UserInfoCallback
import me.echeung.moemoekyun.client.api.data.transform
import me.echeung.moemoekyun.client.api.library.Library
import me.echeung.moemoekyun.client.auth.AuthUtil
import me.echeung.moemoekyun.client.cache.SongsCache
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.client.model.User
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

class APIClient(okHttpClient: OkHttpClient, private val authUtil: AuthUtil) {

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
                        if (authUtil.mfaToken != null) {
                            builder.header("Authorization", authUtil.mfaAuthTokenWithPrefix)
                        }

                        // Authorized calls
                        if (authUtil.isAuthenticated) {
                            builder.header("Authorization", authUtil.authTokenWithPrefix)
                        }

                        return chain.proceed(builder.build())
                    }
                })
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build()

        val cacheFile = File(App.context.filesDir, "apolloCache")
        val cacheSize = 1024 * 1024.toLong()
        val cacheStore = DiskLruHttpCacheStore(cacheFile, cacheSize)

        client = ApolloClient.builder()
            .serverUrl(Library.API_BASE)
            .httpCache(ApolloHttpCache(cacheStore))
            .defaultHttpCachePolicy(DEFAULT_CACHE_POLICY)
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
                            authUtil.mfaToken = userToken
                            callback.onMfaRequired(userToken)
                            return
                        }

                        authUtil.authToken = userToken
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
                        authUtil.authToken = userToken
                        authUtil.clearMfaAuthToken()
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
                        if (response.hasErrors()) {
                            callback.onFailure(response.errors()[0]?.message())
                            return
                        }

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
     * Gets details for a song.
     *
     * @param songId Song to get details for.
     * @param callback Listener to handle the response.
     */
    fun getSongDetails(songId: Int, callback: SongCallback) {
        client.query(SongQuery(songId))
                .httpCachePolicy(SONG_CACHE_POLICY)
                .enqueue(object : ApolloCall.Callback<SongQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        callback.onFailure(e.message)
                    }

                    override fun onResponse(response: Response<SongQuery.Data>) {
                        callback.onSuccess(response.data()?.song!!.transform())
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
        // TODO: maintain an actual DB of song info so we don't need to query as much stuff
        client.query(SongsQuery(0, 50000, Input.optional(RadioClient.isKpop())))
                .httpCachePolicy(SONG_CACHE_POLICY)
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

    /**
     * Gets and subscribes to song queue info.
     *
     * @param callback Listener to handle the response.
     */
    fun getQueue(user: User, callback: QueueCallback) {
        client.query(QueueQuery())
                .enqueue(object : ApolloCall.Callback<QueueQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        callback.onFailure(e.message)
                    }

                    override fun onResponse(response: Response<QueueQuery.Data>) {
                        callback.onQueueSuccess(response.data()?.queue ?: 0)
                    }
                })

        client.subscribe(QueueSubscription(RadioClient.library!!.name))
                .execute(object : ApolloSubscriptionCall.Callback<QueueSubscription.Data> {
                    override fun onFailure(e: ApolloException) {
                        callback.onFailure(e.message)
                    }

                    override fun onResponse(response: Response<QueueSubscription.Data>) {
                        callback.onQueueSuccess(response.data()?.queue?.amount ?: 0)
                    }

                    override fun onConnected() {
                    }

                    override fun onTerminated() {
                    }

                    override fun onCompleted() {
                    }
                })

        // TODO: handle user change
        client.subscribe(UserQueueSubscription(RadioClient.library!!.name, user.uuid))
                .execute(object : ApolloSubscriptionCall.Callback<UserQueueSubscription.Data> {
                    override fun onFailure(e: ApolloException) {

                    }

                    override fun onResponse(response: Response<UserQueueSubscription.Data>) {
                        callback.onUserQueueSuccess(
                                response.data()?.userQueue?.amount ?: 0,
                                response.data()?.userQueue?.before ?: 0
                        )
                    }

                    override fun onConnected() {

                    }

                    override fun onTerminated() {

                    }

                    override fun onCompleted() {

                    }
                })
    }

    private fun filterSongs(songs: List<Song>, query: String?): List<Song> {
        return songs.asSequence()
                .filter { song -> song.search(query) }
                .toList()
    }

    companion object {
        private val DEFAULT_CACHE_POLICY = HttpCachePolicy.NETWORK_FIRST
        private val SONG_CACHE_POLICY = HttpCachePolicy.CACHE_FIRST.expireAfter(1, TimeUnit.DAYS)
    }
}
