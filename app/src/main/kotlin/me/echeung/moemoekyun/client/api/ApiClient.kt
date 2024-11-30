package me.echeung.moemoekyun.client.api

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.cache.http.HttpFetchPolicy
import com.apollographql.apollo.cache.http.httpExpireTimeout
import com.apollographql.apollo.cache.http.httpFetchPolicy
import logcat.LogPriority
import logcat.logcat
import me.echeung.moemoekyun.FavoriteMutation
import me.echeung.moemoekyun.FavoritesQuery
import me.echeung.moemoekyun.LoginMfaMutation
import me.echeung.moemoekyun.LoginMutation
import me.echeung.moemoekyun.RegisterMutation
import me.echeung.moemoekyun.RequestSongMutation
import me.echeung.moemoekyun.SongQuery
import me.echeung.moemoekyun.SongsQuery
import me.echeung.moemoekyun.UserQuery
import me.echeung.moemoekyun.client.api.data.toMessage
import me.echeung.moemoekyun.client.api.data.transform
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.client.model.User
import me.echeung.moemoekyun.util.PreferenceUtil
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiClient @Inject constructor(
    private val client: ApolloClient,
    private val preferenceUtil: PreferenceUtil,
) {

    /**
     * Authenticates to the radio.
     *
     * @param username User's username.
     * @param password User's password.
     */
    suspend fun authenticate(username: String, password: String): Pair<LoginResult, String> {
        val response = client.mutation(LoginMutation(username, password)).execute()
        try {
            val userToken = response.data?.login?.token!!

            if (response.data?.login?.mfa!!) {
                return Pair(LoginResult.REQUIRE_OTP, userToken)
            }

            return Pair(LoginResult.COMPLETE, userToken)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR) { "Failed to authenticate" }
            return Pair(LoginResult.ERROR, response.errors.toMessage())
        }
    }

    /**
     * Second step for MFA authentication.
     *
     * @param otpToken User's one-time password token.
     */
    suspend fun authenticateMfa(otpToken: String): Pair<LoginResult, String> {
        val response = client.mutation(LoginMfaMutation(otpToken)).execute()
        return try {
            Pair(LoginResult.COMPLETE, response.data?.loginMFA?.token!!)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR) { "Failed to authenticate (MFA)" }
            return Pair(LoginResult.ERROR, response.errors.toMessage())
        }
    }

    /**
     * Register a new user.
     */
    suspend fun register(email: String, username: String, password: String) {
        val result = client.mutation(RegisterMutation(email, username, password)).execute()

        if (result.hasErrors()) {
            throw IllegalStateException(result.errors.toMessage())
        }
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
            FavoritesQuery("@me", 0, 2500, Optional.presentIfNotNull(preferenceUtil.station().get() == Station.KPOP)),
        ).execute()

        return response.data?.user?.favorites?.favorites
            ?.mapNotNull { it?.transform() }
            .orEmpty()
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
            RequestSongMutation(songId, Optional.presentIfNotNull(preferenceUtil.station().get() == Station.KPOP)),
        ).execute()

        if (response.hasErrors()) {
            throw Exception(response.errors.toMessage())
        }
    }

    /**
     * Gets details for a song.
     *
     * @param songId Song to get details for.
     */
    suspend fun getSongDetails(songId: Int): Song {
        val response = client.query(SongQuery(songId))
            .httpFetchPolicy(HttpFetchPolicy.CacheFirst)
            .httpExpireTimeout(TimeUnit.DAYS.toMillis(1))
            .execute()

        return response.data?.song!!.transform()
    }

    /**
     * Gets all songs.
     */
    suspend fun getAllSongs(): List<Song> {
        // TODO: maintain an actual DB of song info so we don't need to query as much stuff
        val pageSize = 15_000
        var offset = 0
        val songs = mutableListOf<Song>()
        var response: ApolloResponse<SongsQuery.Data>

        do {
            response = client.query(
                SongsQuery(offset, pageSize, Optional.presentIfNotNull(preferenceUtil.station().get() == Station.KPOP)),
            )
                .httpFetchPolicy(HttpFetchPolicy.CacheFirst)
                .httpExpireTimeout(TimeUnit.DAYS.toMillis(1))
                .execute()

            songs.addAll(response.data?.songs?.songs?.map { it.transform() }.orEmpty())

            offset += pageSize
        } while (offset < (response.data?.songs?.count ?: 0))

        return songs
    }

    enum class LoginResult {
        COMPLETE,
        REQUIRE_OTP,
        ERROR,
    }
}
