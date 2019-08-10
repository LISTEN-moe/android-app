package me.echeung.moemoekyun.client.api

import me.echeung.moemoekyun.BuildConfig
import me.echeung.moemoekyun.client.api.callback.FavoriteSongCallback
import me.echeung.moemoekyun.client.api.callback.IsFavoriteCallback
import me.echeung.moemoekyun.client.api.callback.LoginCallback
import me.echeung.moemoekyun.client.api.callback.RegisterCallback
import me.echeung.moemoekyun.client.api.callback.RequestSongCallback
import me.echeung.moemoekyun.client.api.callback.SearchCallback
import me.echeung.moemoekyun.client.api.callback.SongsCallback
import me.echeung.moemoekyun.client.api.callback.UserFavoritesCallback
import me.echeung.moemoekyun.client.api.callback.UserInfoCallback
import me.echeung.moemoekyun.client.api.v4.APIClient4
import me.echeung.moemoekyun.client.api.v5.APIClient5
import me.echeung.moemoekyun.client.auth.AuthTokenUtil
import okhttp3.OkHttpClient

/**
 * Ghetto API client for selectively using the v4 or v5 APIs.
 */
class BridgeAPIClient(okHttpClient: OkHttpClient, authUtil: AuthTokenUtil) : APIClient {

    private val api4: APIClient4 = APIClient4(okHttpClient, authUtil)
    private val api5: APIClient5 = APIClient5(okHttpClient, authUtil)

    private val api: APIClient
        get() {
            return if (BuildConfig.USE_V5_API) api5 else api4
        }

    override fun authenticate(username: String, password: String, callback: LoginCallback) {
        api.authenticate(username, password, callback)
    }

    override fun authenticateMfa(otpToken: String, callback: LoginCallback) {
        api.authenticateMfa(otpToken, callback)
    }

    override fun register(email: String, username: String, password: String, callback: RegisterCallback) {
        api.register(email, username, password, callback)
    }

    override fun getUserInfo(callback: UserInfoCallback) {
        api.getUserInfo(callback)
    }

    override fun getUserFavorites(callback: UserFavoritesCallback) {
        api.getUserFavorites(callback)
    }

    override fun isFavorite(songIds: List<Int>, callback: IsFavoriteCallback) {
        api.isFavorite(songIds, callback)
    }

    override fun toggleFavorite(songId: Int, isFavorite: Boolean, callback: FavoriteSongCallback) {
        api.toggleFavorite(songId, isFavorite, callback)
    }

    override fun requestSong(songId: Int, callback: RequestSongCallback) {
        api.requestSong(songId, callback)
    }

    override fun getSongs(callback: SongsCallback) {
        api.getSongs(callback)
    }

    override fun search(query: String?, callback: SearchCallback) {
        api.search(query, callback)
    }
}
