package me.echeung.moemoekyun.client.api.v4.callback

import me.echeung.moemoekyun.client.model.Song

interface SearchCallback : BaseCallback {
    fun onSuccess(favorites: List<Song>)
}
