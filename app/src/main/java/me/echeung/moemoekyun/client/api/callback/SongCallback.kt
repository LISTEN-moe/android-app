package me.echeung.moemoekyun.client.api.callback

import me.echeung.moemoekyun.client.model.Song

interface SongCallback : BaseCallback {
    fun onSuccess(song: Song)
}
