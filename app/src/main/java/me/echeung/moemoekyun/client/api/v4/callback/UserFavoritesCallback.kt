package me.echeung.moemoekyun.client.api.v4.callback

import me.echeung.moemoekyun.client.model.Song

interface UserFavoritesCallback : BaseCallback {
    fun onSuccess(favorites: List<Song>)
}
