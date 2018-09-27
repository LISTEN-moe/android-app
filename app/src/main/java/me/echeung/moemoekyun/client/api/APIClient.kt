package me.echeung.moemoekyun.client.api

import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.client.api.cache.SongsCache
import me.echeung.moemoekyun.client.api.callback.*
import me.echeung.moemoekyun.client.api.library.Library
import me.echeung.moemoekyun.client.api.response.*
import me.echeung.moemoekyun.client.api.service.*
import me.echeung.moemoekyun.client.auth.AuthUtil
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.client.model.SongListItem
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*

class APIClient(okHttpClient: OkHttpClient, private val authUtil: AuthUtil) {

    private val artistsService: ArtistsService
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

        artistsService = retrofit.create(ArtistsService::class.java)
        authService = retrofit.create(AuthService::class.java)
        favoritesService = retrofit.create(FavoritesService::class.java)
        requestsService = retrofit.create(RequestsService::class.java)
        songsService = retrofit.create(SongsService::class.java)
        usersService = retrofit.create(UsersService::class.java)

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
        authService.login(AuthService.LoginBody(username, password))
                .enqueue(object : ErrorHandlingAdapter.WrappedCallback<AuthResponse>(callback) {
                    override fun success(response: AuthResponse?) {
                        val userToken = response!!.token

                        if (response.isMfa) {
                            authUtil.setMfaAuthToken(userToken!!)
                            callback.onMfaRequired(userToken)
                            return
                        }

                        authUtil.authToken = userToken
                        callback.onSuccess(userToken!!)
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
        authService.mfa(authUtil.mfaAuthTokenWithPrefix, AuthService.LoginMfaBody(otpToken))
                .enqueue(object : ErrorHandlingAdapter.WrappedCallback<AuthResponse>(callback) {
                    override fun success(response: AuthResponse?) {
                        val userToken = response!!.token
                        authUtil.authToken = userToken
                        authUtil.clearMfaAuthToken()
                        callback.onSuccess(userToken!!)
                    }
                })
    }

    /**
     * Register a new user.
     *
     * @param callback Listener to handle the response.
     */
    fun register(email: String, username: String, password: String, callback: RegisterCallback) {
        authService.register(AuthService.RegisterBody(email, username, password))
                .enqueue(object : ErrorHandlingAdapter.WrappedCallback<BaseResponse>(callback) {
                    override fun success(response: BaseResponse?) {
                        callback.onSuccess(response!!.message!!)
                    }
                })
    }

    /**
     * Gets the user information (id and username).
     *
     * @param callback Listener to handle the response.
     */
    fun getUserInfo(callback: UserInfoCallback) {
        if (!authUtil.isAuthenticated) {
            callback.onFailure(AUTH_ERROR)
            return
        }

        usersService.getUserInfo(authUtil.authTokenWithPrefix, "@me")
                .enqueue(object : ErrorHandlingAdapter.WrappedCallback<UserResponse>(callback) {
                    override fun success(response: UserResponse?) {
                        callback.onSuccess(response!!.user!!)
                    }
                })
    }

    /**
     * Gets a list of all the user's favorited songs.
     *
     * @param callback Listener to handle the response.
     */
    fun getUserFavorites(callback: UserFavoritesCallback) {
        if (!authUtil.isAuthenticated) {
            callback.onFailure(AUTH_ERROR)
            return
        }

        favoritesService.getFavorites(authUtil.authTokenWithPrefix, RadioClient.library!!.name, "@me")
                .enqueue(object : ErrorHandlingAdapter.WrappedCallback<FavoritesResponse>(callback) {
                    override fun success(response: FavoritesResponse?) {
                        val favorites = response!!.favorites
                        for (song in favorites!!) {
                            song.isFavorite = true
                        }
                        callback.onSuccess(favorites)
                    }
                })
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
     * @param songId   Song to unfavorite.
     * @param callback Listener to handle the response.
     */
    fun unfavoriteSong(songId: String, callback: FavoriteSongCallback) {
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
     * @param songId   Song to request.
     * @param callback Listener to handle the response.
     */
    fun requestSong(songId: String, callback: RequestSongCallback) {
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
    fun getSongs(callback: SongsCallback) {
        if (!authUtil.isAuthenticated) {
            callback.onFailure(AUTH_ERROR)
            return
        }

        songsService.getSongs(authUtil.authTokenWithPrefix, RadioClient.library!!.name)
                .enqueue(object : ErrorHandlingAdapter.WrappedCallback<SongsResponse>(callback) {
                    override fun success(response: SongsResponse?) {
                        callback.onSuccess(response!!.songs!!)
                    }
                })
    }

    /**
     * Searches for songs.
     *
     * @param query    Search query string.
     * @param callback Listener to handle the response.
     */
    fun search(query: String?, callback: SearchCallback) {
        if (!authUtil.isAuthenticated) {
            callback.onFailure(AUTH_ERROR)
            return
        }

        songsCache.getSongs(object : SongsCache.Callback {
            override fun onRetrieve(songs: List<SongListItem>?) {
                val filteredSongs = filterSongs(songs!!, query)
                callback.onSuccess(filteredSongs)
            }

            override fun onFailure(message: String) {
                callback.onFailure(message)
            }
        })
    }

    /**
     * Gets a list of all artists.
     *
     * @param callback Listener to handle the response.
     */
    fun getArtists(callback: ArtistsCallback) {
        if (!authUtil.isAuthenticated) {
            callback.onFailure(AUTH_ERROR)
            return
        }

        artistsService.getArtists(authUtil.authTokenWithPrefix, RadioClient.library!!.name)
                .enqueue(object : ErrorHandlingAdapter.WrappedCallback<ArtistsResponse>(callback) {
                    override fun success(response: ArtistsResponse?) {
                        callback.onSuccess(response!!.artists!!)
                    }
                })
    }

    /**
     * Gets an artist's info.
     *
     * @param artistId Artist to get.
     * @param callback Listener to handle the response.
     */
    fun getArtist(artistId: String, callback: ArtistCallback) {
        if (!authUtil.isAuthenticated) {
            callback.onFailure(AUTH_ERROR)
            return
        }

        artistsService.getArtist(authUtil.authTokenWithPrefix, RadioClient.library!!.name, artistId)
                .enqueue(object : ErrorHandlingAdapter.WrappedCallback<ArtistResponse>(callback) {
                    override fun success(response: ArtistResponse?) {
                        callback.onSuccess(response!!.artist!!)
                    }
                })
    }

    private fun filterSongs(songs: List<SongListItem>, query: String?): List<Song> {
        val filteredSongs = ArrayList<Song>()

        for (song in songs) {
            if (query == null || song.search(query)) {
                filteredSongs.add(SongListItem.toSong(song))
            }
        }

        return filteredSongs
    }

    companion object {
        // TODO: better handle this
        const val AUTH_ERROR = "api-auth-error"

        lateinit var retrofit: Retrofit
    }

}
