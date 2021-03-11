package me.echeung.moemoekyun.client.api

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.cache.http.ApolloHttpCache
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toFlow
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
import me.echeung.moemoekyun.client.api.data.SongsCache
import me.echeung.moemoekyun.client.api.data.transform
import me.echeung.moemoekyun.client.api.library.Library
import me.echeung.moemoekyun.client.auth.AuthUtil
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.client.model.User
import me.echeung.moemoekyun.client.model.search
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class APIClient(
    okHttpClient: OkHttpClient,
    apolloCache: ApolloHttpCache,
    private val authUtil: AuthUtil
) {

    private val scope = MainScope()

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
     */
    suspend fun authenticate(username: String, password: String): Pair<LoginState, String> {
        val response = client.mutate(LoginMutation(username, password)).await()

        val userToken = response.data?.login?.token!!

        if (response.data?.login?.mfa!!) {
            authUtil.mfaToken = userToken
            return Pair(LoginState.REQUIRE_OTP, userToken)
        }

        authUtil.authToken = userToken

        return Pair(LoginState.COMPLETE, userToken)
    }

    /**
     * Second step for MFA authentication.
     *
     * @param otpToken User's one-time password token.
     */
    suspend fun authenticateMfa(otpToken: String): String {
        val response = client.mutate(LoginMfaMutation(otpToken)).await()

        val userToken = response.data?.loginMFA?.token!!
        authUtil.authToken = userToken
        authUtil.clearMfaAuthToken()

        return userToken
    }

    /**
     * Register a new user.
     */
    suspend fun register(email: String, username: String, password: String) {
        client.mutate(RegisterMutation(email, username, password)).await()
    }

    /**
     * Gets the user information (id and username).
     */
    suspend fun getUserInfo(): User {
        val response = client.query(UserQuery("@me")).await()

        return response.data?.user!!.transform()
    }

    /**
     * Gets a list of all the user's favorited songs.
     */
    suspend fun getUserFavorites(): List<Song> {
        // TODO: do actual pagination
        val response = client.query(FavoritesQuery("@me", 0, 2500, Input.optional(RadioClient.isKpop()))).await()

        return response.data?.user?.favorites?.favorites
            ?.mapNotNull { it?.song }
            ?.map { it.transform() }
            ?: emptyList()
    }

    /**
     * Gets the favorited status of a list of songs.
     *
     * @param songIds IDs of songs to check status of.
     */
    suspend fun isFavorite(songIds: List<Int>): List<Int> {
        val response = client.query(CheckFavoriteQuery(songIds)).await()

        return response.data?.checkFavorite?.filterNotNull() ?: emptyList()
    }

    /**
     * Toggles a song's favorite status
     *
     * @param songId Song to update favorite status of.
     */
    suspend fun toggleFavorite(songId: Int) {
        client.mutate(FavoriteMutation(songId)).await()
    }

    /**
     * Sends a song request to the queue.
     *
     * @param songId Song to request.
     */
    suspend fun requestSong(songId: Int) {
        val response = client.mutate(RequestSongMutation(songId, Input.optional(RadioClient.isKpop()))).await()

        if (response.hasErrors()) {
            throw Exception(response.errors?.get(0)?.message)
        }
    }

    /**
     * Searches for songs.
     *
     * @param query Search query string.
     */
    suspend fun search(query: String?): List<Song> {
        val songs = songsCache.getSongs()

        return songs!!.search(query)
    }

    /**
     * Gets details for a song.
     *
     * @param songId Song to get details for.
     */
    suspend fun getSongDetails(songId: Int): Song {
        val response = client.query(SongQuery(songId))
            .httpCachePolicy(SONG_CACHE_POLICY).await()

        return response.data?.song!!.transform()
    }

    /**
     * Gets all songs.
     */
    suspend fun getAllSongs(): List<Song> {
        // TODO: do actual pagination
        // TODO: maintain an actual DB of song info so we don't need to query as much stuff
        val response = client.query(SongsQuery(0, 50000, Input.optional(RadioClient.isKpop())))
            .httpCachePolicy(SONG_CACHE_POLICY).await()

        return response.data?.songs?.songs?.map { it.transform() } ?: emptyList()
    }

    /**
     * Gets and subscribes to song queue info.
     */
    suspend fun getQueue(user: User) {
        val queue = client.query(QueueQuery()).await()

//           callback.onQueueSuccess(response.data?.queue ?: 0)

        client.subscribe(QueueSubscription(RadioClient.library.name))
            .toFlow()
            .onEach {
//                    callback.onQueueSuccess(response.data?.queue?.amount ?: 0)
            }
            .launchIn(scope)

        // TODO: handle user change
        client.subscribe(UserQueueSubscription(RadioClient.library.name, user.uuid))
            .toFlow()
            .onEach {
//                    callback.onUserQueueSuccess(
//                            response.data?.userQueue?.amount ?: 0,
//                            response.data?.userQueue?.before ?: 0
//                    )
            }
            .launchIn(scope)
    }

    enum class LoginState {
        REQUIRE_OTP,
        COMPLETE,
    }

    companion object {
        private val DEFAULT_CACHE_POLICY = HttpCachePolicy.NETWORK_FIRST
        private val SONG_CACHE_POLICY = HttpCachePolicy.CACHE_FIRST.expireAfter(1, TimeUnit.DAYS)
    }
}
