package me.echeung.moemoekyun.client.api.v5

import android.util.Log
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
import me.echeung.moemoekyun.SearchQuery
import me.echeung.moemoekyun.SongsQuery
import me.echeung.moemoekyun.UserQuery
import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.client.api.APIClient
import me.echeung.moemoekyun.client.api.callback.FavoriteSongCallback
import me.echeung.moemoekyun.client.api.callback.IsFavoriteCallback
import me.echeung.moemoekyun.client.api.callback.LoginCallback
import me.echeung.moemoekyun.client.api.callback.RegisterCallback
import me.echeung.moemoekyun.client.api.callback.RequestSongCallback
import me.echeung.moemoekyun.client.api.callback.SearchCallback
import me.echeung.moemoekyun.client.api.callback.SongsCallback
import me.echeung.moemoekyun.client.api.callback.UserFavoritesCallback
import me.echeung.moemoekyun.client.api.callback.UserInfoCallback
import me.echeung.moemoekyun.client.auth.AuthUtil
import okhttp3.OkHttpClient

class APIClient5(okHttpClient: OkHttpClient, private val authUtil: AuthUtil) : APIClient {

    private val client: ApolloClient

    init {
        // Automatically add auth token to requests
        val authClient = okHttpClient.newBuilder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val builder = original.newBuilder().method(original.method(), original.body())
                    if (authUtil.isAuthenticated) {
                        builder.header("Authorization", authUtil.authTokenWithPrefix)
                    }
                    chain.proceed(builder.build())
                }
                .build()

        client = ApolloClient.builder()
            .serverUrl(Library.API_BASE)
            .okHttpClient(authClient)
            .build()
    }

    /**
     * Authenticates to the radio.
     *
     * @param username User's username.
     * @param password User's password.
     * @param callback Listener to handle the response.
     */
    override fun authenticate(username: String, password: String, callback: LoginCallback) {
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
    override fun authenticateMfa(otpToken: String, callback: LoginCallback) {
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
    override fun register(email: String, username: String, password: String, callback: RegisterCallback) {
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
    override fun getUserInfo(callback: UserInfoCallback) {
        client.query(UserQuery("@me"))
                .enqueue(object : ApolloCall.Callback<UserQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        callback.onFailure(e.message)
                    }

                    override fun onResponse(response: Response<UserQuery.Data>) {
                        Log.d("GraphQL response", response.data()?.user?.username)
//                        callback.onSuccess(response.data()?.user!!)
                    }
                })
    }

    /**
     * Gets a list of all the user's favorited songs.
     *
     * @param callback Listener to handle the response.
     */
    override fun getUserFavorites(callback: UserFavoritesCallback) {
        client.query(FavoritesQuery("@me"))
                .enqueue(object : ApolloCall.Callback<FavoritesQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        callback.onFailure(e.message)
                    }

                    override fun onResponse(response: Response<FavoritesQuery.Data>) {
                        Log.d("GraphQL response", response.data()?.user?.favorites?.favorites?.toString())
//                        callback.onSuccess(response.data()?.user?.favorites?.favorites!!)
                    }
                })
    }

    override fun isFavorite(songIds: List<Int>, callback: IsFavoriteCallback) {
        client.query(CheckFavoriteQuery(listOf(1, 2, 3)))
                .enqueue(object : ApolloCall.Callback<CheckFavoriteQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
//                        callback.onFailure(e.message)
                    }

                    override fun onResponse(response: Response<CheckFavoriteQuery.Data>) {
//                        callback.onSuccess(response.data()?.checkFavorite)
                    }
                })
    }

    /**
     * Toggles a song's favorite status
     *
     * @param songId Song to update favorite status of.
     * @param isFavorite Whether the song is currently favorited.
     * @param callback Listener to handle the response.
     */
    override fun toggleFavorite(songId: Int, isFavorite: Boolean, callback: FavoriteSongCallback) {
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
    override fun requestSong(songId: Int, callback: RequestSongCallback) {
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
     * Gets all songs.
     *
     * @param callback Listener to handle the response.
     */
    override fun getSongs(callback: SongsCallback) {
        // TODO: do actual pagination
        client.query(SongsQuery(0, 50000, Input.optional(RadioClient.isKpop())))
                .enqueue(object : ApolloCall.Callback<SongsQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        callback.onFailure(e.message)
                    }

                    override fun onResponse(response: Response<SongsQuery.Data>) {
                        Log.d("GraphQL response", response.data()?.songs?.songs?.toString())
                    }
                })
    }

    /**
     * Searches for songs.
     *
     * @param query Search query string.
     * @param callback Listener to handle the response.
     */
    override fun search(query: String?, callback: SearchCallback) {
        client.query(SearchQuery(query!!))
                .enqueue(object : ApolloCall.Callback<SearchQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        callback.onFailure(e.message)
                    }

                    override fun onResponse(response: Response<SearchQuery.Data>) {
                        Log.d("GraphQL response", response.data()?.search?.toString())
//                        callback.onSuccess(response.data()?.search!!)
                    }
                })
    }

}
