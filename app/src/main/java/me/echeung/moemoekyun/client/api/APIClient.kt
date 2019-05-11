package me.echeung.moemoekyun.client.api

import me.echeung.moemoekyun.client.api.callback.FavoriteSongCallback
import me.echeung.moemoekyun.client.api.callback.LoginCallback
import me.echeung.moemoekyun.client.api.callback.RegisterCallback
import me.echeung.moemoekyun.client.api.callback.RequestSongCallback
import me.echeung.moemoekyun.client.api.callback.SearchCallback
import me.echeung.moemoekyun.client.api.callback.SongsCallback
import me.echeung.moemoekyun.client.api.callback.UserFavoritesCallback
import me.echeung.moemoekyun.client.api.callback.UserInfoCallback

interface APIClient {

    fun authenticate(username: String, password: String, callback: LoginCallback)

    fun authenticateMfa(otpToken: String, callback: LoginCallback)

    fun register(email: String, username: String, password: String, callback: RegisterCallback)

    fun getUserInfo(callback: UserInfoCallback)

    fun getUserFavorites(callback: UserFavoritesCallback)

    fun toggleFavorite(songId: String, isFavorite: Boolean, callback: FavoriteSongCallback)

    fun favoriteSong(songId: String, callback: FavoriteSongCallback)

    fun unfavoriteSong(songId: String, callback: FavoriteSongCallback)

    fun requestSong(songId: String, callback: RequestSongCallback)

    fun getSongs(callback: SongsCallback)

    fun search(query: String?, callback: SearchCallback)

}
