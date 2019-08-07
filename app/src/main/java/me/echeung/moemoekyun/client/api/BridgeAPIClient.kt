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
        api4.authenticate(username, password, callback)
    }

    override fun authenticateMfa(otpToken: String, callback: LoginCallback) {
        api4.authenticateMfa(otpToken, callback)
    }

    override fun register(email: String, username: String, password: String, callback: RegisterCallback) {
        api4.register(email, username, password, callback)
    }

    override fun getUserInfo(callback: UserInfoCallback) {
        api4.getUserInfo(callback)
    }

    override fun getUserFavorites(callback: UserFavoritesCallback) {
        api4.getUserFavorites(callback)
    }

    override fun isFavorite(songIds: List<Int>, callback: IsFavoriteCallback) {
        api4.isFavorite(songIds, callback)
    }

    override fun toggleFavorite(songId: Int, isFavorite: Boolean, callback: FavoriteSongCallback) {
        api4.toggleFavorite(songId, isFavorite, callback)
    }

    override fun requestSong(songId: Int, callback: RequestSongCallback) {
        api4.requestSong(songId, callback)
    }

    override fun getSongs(callback: SongsCallback) {
        api4.getSongs(callback)
    }

    override fun search(query: String?, callback: SearchCallback) {
        api4.search(query, callback)
    }
}
