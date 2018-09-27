package me.echeung.moemoekyun.client.api.callback

import me.echeung.moemoekyun.client.model.Song

interface SearchCallback : BaseCallback {
    fun onSuccess(favorites: List<Song>)
}
