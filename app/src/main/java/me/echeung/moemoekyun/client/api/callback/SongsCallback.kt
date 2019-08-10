package me.echeung.moemoekyun.client.api.callback

import me.echeung.moemoekyun.client.model.Song

interface SongsCallback : BaseCallback {
    fun onSuccess(songs: List<Song>)
}
