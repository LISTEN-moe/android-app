package me.echeung.moemoekyun.client.api

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.ApolloSubscriptionCall
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.cache.http.ApolloHttpCache
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.exception.ApolloException
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
import me.echeung.moemoekyun.client.api.callback.UserFavoritesCallback
import me.echeung.moemoekyun.client.api.callback.UserInfoCallback
import me.echeung.moemoekyun.client.api.data.transform
import me.echeung.moemoekyun.client.api.library.Library
import me.echeung.moemoekyun.client.auth.AuthUtil
import me.echeung.moemoekyun.client.cache.SongsCache
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.client.model.User
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class APIClient(
        okHttpClient: OkHttpClient,
        apolloCache: ApolloHttpCache,
        private val authUtil: AuthUtil
) {

    private val client: ApolloClient
    private val songsCache: SongsCache

    init {
//        val transportFactory = WebSocketSubscriptionTransport.Factory(webSocketUrl, okHttpClient)

        client = ApolloClient.builder()
                .serverUrl(Library.API_BASE)
                .httpCache(apolloCache)
                .defaultHttpCachePolicy(DEFAULT_CACHE_POLICY)
                .okHttpClient(okHttpClient)
//            .subscriptionTransportFactory(transportFactory)
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
    suspend fun authenticate(username: String, password: String, callback: LoginCallback) {
        try {
            val response =  client.mutate(LoginMutation(username, password))
                    .toDeferred()
                    .await()

            val userToken = response.data()?.login?.token!!

            if (response.data()?.login?.mfa!!) {
                authUtil.mfaToken = userToken
                callback.onMfaRequired(userToken)
                return
            }

            authUtil.authToken = userToken
            callback.onSuccess(userToken)
        } catch (e: Exception) {
            callback.onFailure(e.message)
        }
    }

    /**
     * Second step for MFA authentication.
     *
     * @param otpToken User's one-time password token.
     * @param callback Listener to handle the response.
     */
    suspend fun authenticateMfa(otpToken: String, callback: LoginCallback) {
        try {
            val response = client.mutate(LoginMfaMutation(otpToken))
                    .toDeferred()
                    .await()

            val userToken = response.data()?.loginMFA?.token!!
            authUtil.authToken = userToken
            authUtil.clearMfaAuthToken()
            callback.onSuccess(userToken)
        } catch (e: Exception) {
            callback.onFailure(e.message)
        }
    }

    /**
     * Register a new user.
     *
     * @param callback Listener to handle the response.
     */
    suspend fun register(email: String, username: String, password: String, callback: RegisterCallback) {
        try {
            client.mutate(RegisterMutation(email, username, password))
                    .toDeferred()
                    .await()

            callback.onSuccess()
        } catch (e: Exception) {
            callback.onFailure(e.message)
        }
    }

    /**
     * Gets the user information (id and username).
     *
     * @param callback Listener to handle the response.
     */
    suspend fun getUserInfo(callback: UserInfoCallback) {
        try {
            val response = client.query(UserQuery("@me"))
                    .toDeferred()
                    .await()

            callback.onSuccess(response.data()?.user!!.transform())
        } catch (e: Exception) {
            callback.onFailure(e.message)
        }
    }

    /**
     * Gets a list of all the user's favorited songs.
     *
     * @param callback Listener to handle the response.
     */
    suspend fun getUserFavorites(callback: UserFavoritesCallback) {
        try {
            // TODO: do actual pagination
            val response = client.query(FavoritesQuery("@me", 0, 2500, Input.optional(RadioClient.isKpop())))
                    .toDeferred()
                    .await()

            callback.onSuccess(
                    response.data()?.user?.favorites?.favorites
                            ?.mapNotNull { it?.song }
                            ?.map { it.transform() }
                            ?: emptyList())
        } catch (e: Exception) {
            callback.onFailure(e.message)
        }
    }

    /**
     * Gets the favorited status of a list of songs.
     *
     * @param songIds IDs of songs to check status of.
     * @param callback Listener to handle the response.
     */
    suspend fun isFavorite(songIds: List<Int>, callback: IsFavoriteCallback) {
        try {
            val response = client.query(CheckFavoriteQuery(songIds))
                    .toDeferred()
                    .await()

            callback.onSuccess(response.data()?.checkFavorite?.filterNotNull() ?: emptyList())
        } catch (e: Exception) {
            callback.onFailure(e.message)
        }
    }

    /**
     * Toggles a song's favorite status
     *
     * @param songId Song to update favorite status of.
     * @param callback Listener to handle the response.
     */
    suspend fun toggleFavorite(songId: Int, callback: FavoriteSongCallback) {
        try {
            client.mutate(FavoriteMutation(songId))
                    .toDeferred()
                    .await()

            callback.onSuccess()
        } catch (e: Exception) {
            callback.onFailure(e.message)
        }
    }

    /**
     * Sends a song request to the queue.
     *
     * @param songId Song to request.
     * @param callback Listener to handle the response.
     */
    suspend fun requestSong(songId: Int, callback: RequestSongCallback) {
        try {
            val response = client.mutate(RequestSongMutation(songId, Input.optional(RadioClient.isKpop())))
                    .toDeferred()
                    .await()

            if (response.hasErrors()) {
                callback.onFailure(response.errors()[0]?.message())
                return
            }

            callback.onSuccess()
        } catch (e: Exception) {
            callback.onFailure(e.message)
        }
    }

    /**
     * Searches for songs.
     *
     * @param query Search query string.
     * @param callback Listener to handle the response.
     */
    suspend fun search(query: String?, callback: SearchCallback) {
        try {
            val songs = songsCache.getSongs()
            val filteredSongs = filterSongs(songs!!, query)
            callback.onSuccess(filteredSongs)
        } catch (e: Exception) {
            callback.onFailure(e.message)
        }
    }

    /**
     * Gets details for a song.
     *
     * @param songId Song to get details for.
     * @param callback Listener to handle the response.
     */
    suspend fun getSongDetails(songId: Int, callback: SongCallback) {
        try {
            val response = client.query(SongQuery(songId))
                    .httpCachePolicy(SONG_CACHE_POLICY)
                    .toDeferred()
                    .await()

            callback.onSuccess(response.data()?.song!!.transform())
        } catch (e: Exception) {
            callback.onFailure(e.message)
        }
    }

    /**
     * Gets all songs.
     *
     * @param callback Listener to handle the response.
     */
    suspend fun getAllSongs(): List<Song> {
        // TODO: do actual pagination
        // TODO: maintain an actual DB of song info so we don't need to query as much stuff
        val response = client.query(SongsQuery(0, 50000, Input.optional(RadioClient.isKpop())))
                .httpCachePolicy(SONG_CACHE_POLICY)
                .toDeferred()
                .await()

        return response.data()?.songs?.songs?.map { it.transform() } ?: emptyList()
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
