package me.echeung.moemoekyun.client.api.v4

import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.client.api.APIClient
import me.echeung.moemoekyun.client.api.callback.FavoriteSongCallback
import me.echeung.moemoekyun.client.api.callback.LoginCallback
import me.echeung.moemoekyun.client.api.callback.RegisterCallback
import me.echeung.moemoekyun.client.api.callback.RequestSongCallback
import me.echeung.moemoekyun.client.api.callback.SearchCallback
import me.echeung.moemoekyun.client.api.callback.SongsCallback
import me.echeung.moemoekyun.client.api.callback.UserFavoritesCallback
import me.echeung.moemoekyun.client.api.callback.UserInfoCallback
import me.echeung.moemoekyun.client.api.v4.cache.SongsCache
import me.echeung.moemoekyun.client.api.v4.library.Library
import me.echeung.moemoekyun.client.api.v4.response.AuthResponse
import me.echeung.moemoekyun.client.api.v4.response.BaseResponse
import me.echeung.moemoekyun.client.api.v4.response.FavoritesResponse
import me.echeung.moemoekyun.client.api.v4.response.SongsResponse
import me.echeung.moemoekyun.client.api.v4.response.UserResponse
import me.echeung.moemoekyun.client.api.v4.service.AuthService
import me.echeung.moemoekyun.client.api.v4.service.FavoritesService
import me.echeung.moemoekyun.client.api.v4.service.RequestsService
import me.echeung.moemoekyun.client.api.v4.service.SongsService
import me.echeung.moemoekyun.client.api.v4.service.UsersService
import me.echeung.moemoekyun.client.auth.AuthUtil
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.client.model.SongListItem
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

class APIClient4(okHttpClient: OkHttpClient, private val authUtil: AuthUtil) : APIClient {

    private val authService: AuthService
    private val favoritesService: FavoritesService
    private val requestsService: RequestsService
    private val songsService: SongsService
    private val usersService: UsersService
    private val songsCache: SongsCache

    init {
        retrofit = Retrofit.Builder()
                .baseUrl(Library.API_BASE)
                .client(okHttpClient)
                .addCallAdapterFactory(ErrorHandlingAdapter.ErrorHandlingCallAdapterFactory())
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

        authService = retrofit.create()
        favoritesService = retrofit.create()
        requestsService = retrofit.create()
        songsService = retrofit.create()
        usersService = retrofit.create()

        songsCache = SongsCache(this)
    }

    /**
     * Authenticates to the radio.
     *
     * @param username User's username.
     * @param password User's password.
     * @param callback Listener to handle the response.
     */
    override fun authenticate(username: String, password: String, callback: LoginCallback) {
        authService.login(AuthService.LoginBody(username, password))
                .enqueue(object : ErrorHandlingAdapter.WrappedCallback<AuthResponse>(callback) {
                    override fun success(response: AuthResponse?) {
                        val userToken = response!!.token

                        if (response.mfa) {
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
        authService.mfa(authUtil.mfaAuthTokenWithPrefix, AuthService.LoginMfaBody(otpToken))
                .enqueue(object : ErrorHandlingAdapter.WrappedCallback<AuthResponse>(callback) {
                    override fun success(response: AuthResponse?) {
                        val userToken = response!!.token
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
        authService.register(AuthService.RegisterBody(email, username, password))
                .enqueue(object : ErrorHandlingAdapter.WrappedCallback<BaseResponse>(callback) {
                    override fun success(response: BaseResponse?) {
                        callback.onSuccess(response!!.message)
                    }
                })
    }

    /**
     * Gets the user information (id and username).
     *
     * @param callback Listener to handle the response.
     */
    override fun getUserInfo(callback: UserInfoCallback) {
        if (!authUtil.isAuthenticated) {
            callback.onFailure(AUTH_ERROR)
            return
        }

        usersService.getUserInfo(authUtil.authTokenWithPrefix, "@me")
                .enqueue(object : ErrorHandlingAdapter.WrappedCallback<UserResponse>(callback) {
                    override fun success(response: UserResponse?) {
                        callback.onSuccess(response!!.user)
                    }
                })
    }

    /**
     * Gets a list of all the user's favorited songs.
     *
     * @param callback Listener to handle the response.
     */
    override fun getUserFavorites(callback: UserFavoritesCallback) {
        if (!authUtil.isAuthenticated) {
            callback.onFailure(AUTH_ERROR)
            return
        }

        favoritesService.getFavorites(authUtil.authTokenWithPrefix, RadioClient.library!!.name, "@me")
                .enqueue(object : ErrorHandlingAdapter.WrappedCallback<FavoritesResponse>(callback) {
                    override fun success(response: FavoritesResponse?) {
                        val favorites = response!!.favorites
                        favorites.forEach { it.favorite = true }
                        callback.onSuccess(favorites)
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
    override fun toggleFavorite(songId: String, isFavorite: Boolean, callback: FavoriteSongCallback) {
        if (isFavorite) {
            unfavoriteSong(songId, callback)
        } else {
            favoriteSong(songId, callback)
        }
    }

    /**
     * Favorites a song.
     *
     * @param songId Song to favorite.
     * @param callback Listener to handle the response.
     */
    override fun favoriteSong(songId: String, callback: FavoriteSongCallback) {
        if (!authUtil.isAuthenticated) {
            callback.onFailure(AUTH_ERROR)
            return
        }

        favoritesService.favorite(authUtil.authTokenWithPrefix, songId)
                .enqueue(object : ErrorHandlingAdapter.WrappedCallback<BaseResponse>(callback) {
                    override fun success(response: BaseResponse?) {
                        callback.onSuccess()
                    }
                })
    }

    /**
     * Unfavorites a song.
     *
     * @param songId Song to unfavorite.
     * @param callback Listener to handle the response.
     */
    override fun unfavoriteSong(songId: String, callback: FavoriteSongCallback) {
        if (!authUtil.isAuthenticated) {
            callback.onFailure(AUTH_ERROR)
            return
        }

        favoritesService.removeFavorite(authUtil.authTokenWithPrefix, songId)
                .enqueue(object : ErrorHandlingAdapter.WrappedCallback<BaseResponse>(callback) {
                    override fun success(response: BaseResponse?) {
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
    override fun requestSong(songId: String, callback: RequestSongCallback) {
        if (!authUtil.isAuthenticated) {
            callback.onFailure(AUTH_ERROR)
            return
        }

        requestsService.request(authUtil.authTokenWithPrefix, RadioClient.library!!.name, songId)
                .enqueue(object : ErrorHandlingAdapter.WrappedCallback<BaseResponse>(callback) {
                    override fun success(response: BaseResponse?) {
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
        if (!authUtil.isAuthenticated) {
            callback.onFailure(AUTH_ERROR)
            return
        }

        songsService.getSongs(authUtil.authTokenWithPrefix, RadioClient.library!!.name)
                .enqueue(object : ErrorHandlingAdapter.WrappedCallback<SongsResponse>(callback) {
                    override fun success(response: SongsResponse?) {
                        callback.onSuccess(response!!.songs)
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
        if (!authUtil.isAuthenticated) {
            callback.onFailure(AUTH_ERROR)
            return
        }

        songsCache.getSongs(object : SongsCache.Callback {
            override fun onRetrieve(songs: List<SongListItem>?) {
                val filteredSongs = filterSongs(songs!!, query)
                callback.onSuccess(filteredSongs)
            }

            override fun onFailure(message: String?) {
                callback.onFailure(message)
            }
        })
    }

    private fun filterSongs(songs: List<SongListItem>, query: String?): List<Song> {
        return songs.asSequence()
                .filter { song -> song.search(query) }
                .map { song -> SongListItem.toSong(song) }
                .toList()
    }

    companion object {
        // TODO: better handle this
        const val AUTH_ERROR = "api-auth-error"

        lateinit var retrofit: Retrofit
    }
}
