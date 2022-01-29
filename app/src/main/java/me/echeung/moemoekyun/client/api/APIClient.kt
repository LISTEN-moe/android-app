package me.echeung.moemoekyun.client.api

import android.content.Context
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.MutableExecutionOptions
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.cache.http.HttpFetchPolicy
import com.apollographql.apollo3.cache.http.httpCache
import com.apollographql.apollo3.cache.http.httpFetchPolicy
import com.apollographql.apollo3.network.okHttpClient
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
import me.echeung.moemoekyun.client.auth.AuthUtil
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.client.model.User
import me.echeung.moemoekyun.client.model.search
import okhttp3.OkHttpClient
import java.io.File

class APIClient(
    context: Context,
    okHttpClient: OkHttpClient,
    private val authUtil: AuthUtil
) {

    private val scope = MainScope()

    private val client: ApolloClient
    private val songsCache: SongsCache

    init {
//        val transportFactory = WebSocketSubscriptionTransport.Factory(webSocketUrl, okHttpClient)

        val cacheFile = File(context.externalCacheDir, "apolloCache")
        val cacheSize = 1024 * 1024.toLong()

        client = ApolloClient.Builder()
            .serverUrl(Library.API_BASE)
            .httpCache(cacheFile, cacheSize)
            .httpFetchPolicy(DEFAULT_CACHE_POLICY)
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
        val response = client.mutation(LoginMutation(username, password)).execute()

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
        val response = client.mutation(LoginMfaMutation(otpToken)).execute()

        val userToken = response.data?.loginMFA?.token!!
        authUtil.authToken = userToken
        authUtil.clearMfaAuthToken()

        return userToken
    }

    /**
     * Register a new user.
     */
    suspend fun register(email: String, username: String, password: String) {
        client.mutation(RegisterMutation(email, username, password)).execute()
    }

    /**
     * Gets the user information (id and username).
     */
    suspend fun getUserInfo(): User {
        val response = client.query(UserQuery("@me")).execute()

        return response.data?.user!!.transform()
    }

    /**
     * Gets a list of all the user's favorited songs.
     */
    suspend fun getUserFavorites(): List<Song> {
        // TODO: do actual pagination
        val response = client.query(
            FavoritesQuery(
                "@me", 0, 2500,
                Optional.presentIfNotNull(RadioClient.isKpop())
            )
        ).execute()

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
        val response = client.query(CheckFavoriteQuery(songIds)).execute()

        return response.data?.checkFavorite?.filterNotNull() ?: emptyList()
    }

    /**
     * Toggles a song's favorite status
     *
     * @param songId Song to update favorite status of.
     */
    suspend fun toggleFavorite(songId: Int) {
        client.mutation(FavoriteMutation(songId)).execute()
    }

    /**
     * Sends a song request to the queue.
     *
     * @param songId Song to request.
     */
    suspend fun requestSong(songId: Int) {
        val response = client.mutation(
            RequestSongMutation(
                songId,
                Optional.presentIfNotNull(RadioClient.isKpop())
            )
        ).execute()

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
            .songCachePolicy()
            .execute()

        return response.data?.song!!.transform()
    }

    /**
     * Gets all songs.
     */
    suspend fun getAllSongs(): List<Song> {
        // TODO: do actual pagination
        // TODO: maintain an actual DB of song info so we don't need to query as much stuff
        val response = client.query(
            SongsQuery(
                0, 50000,
                Optional.presentIfNotNull(RadioClient.isKpop())
            )
        )
            .songCachePolicy()
            .execute()

        return response.data?.songs?.songs?.map { it.transform() } ?: emptyList()
    }

    /**
     * Gets and subscribes to song queue info.
     */
    suspend fun getQueue(user: User) {
        val queue = client.query(QueueQuery()).execute()

//           callback.onQueueSuccess(response.data?.queue ?: 0)

        client.subscription(QueueSubscription(RadioClient.library.name))
            .toFlow()
            .onEach {
//                    callback.onQueueSuccess(response.data?.queue?.amount ?: 0)
            }
            .launchIn(scope)

        // TODO: handle user change
        client.subscription(UserQueueSubscription(RadioClient.library.name, user.uuid))
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
}

private val DEFAULT_CACHE_POLICY = HttpFetchPolicy.NetworkFirst
private fun <T> MutableExecutionOptions<T>.songCachePolicy(): T =
    httpFetchPolicy(HttpFetchPolicy.CacheFirst)
// TODO: add expiration back
// private val SONG_CACHE_POLICY = HttpFetchPolicy.CacheFirst expireAfter(1, TimeUnit.DAYS)
