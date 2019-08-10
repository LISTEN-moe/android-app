package me.echeung.moemoekyun.client.api

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

    override fun authenticate(username: String, password: String, callback: LoginCallback) {
        getApi().authenticate(username, password, callback)
    }

    override fun authenticateMfa(otpToken: String, callback: LoginCallback) {
        getApi().authenticateMfa(otpToken, callback)
    }

    override fun register(email: String, username: String, password: String, callback: RegisterCallback) {
        getApi().register(email, username, password, callback)
    }

    override fun getUserInfo(callback: UserInfoCallback) {
        getApi().getUserInfo(callback)
    }

    override fun getUserFavorites(callback: UserFavoritesCallback) {
        getApi().getUserFavorites(callback)
    }

    override fun isFavorite(songIds: List<Int>, callback: IsFavoriteCallback) {
        getApi().isFavorite(songIds, callback)
    }

    override fun toggleFavorite(songId: Int, isFavorite: Boolean, callback: FavoriteSongCallback) {
        getApi().toggleFavorite(songId, isFavorite, callback)
    }

    override fun requestSong(songId: Int, callback: RequestSongCallback) {
        getApi().requestSong(songId, callback)
    }

    override fun getSongs(callback: SongsCallback) {
        getApi().getSongs(callback)
    }

    override fun search(query: String?, callback: SearchCallback) {
        getApi().search(query, callback)
    }

    private fun getApi(): APIClient {
        return if (useApi5) api5 else api4
    }

    companion object {
        const val useApi5: Boolean = false
    }
}
