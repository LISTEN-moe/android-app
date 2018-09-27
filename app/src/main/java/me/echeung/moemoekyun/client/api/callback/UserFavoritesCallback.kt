package me.echeung.moemoekyun.client.api.callback

import me.echeung.moemoekyun.client.model.Song

interface UserFavoritesCallback : BaseCallback {
    fun onSuccess(favorites: List<Song>)
}
