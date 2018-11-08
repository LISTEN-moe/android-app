package me.echeung.moemoekyun.client.api.v4.callback

import me.echeung.moemoekyun.client.model.SongListItem

interface SongsCallback : BaseCallback {
    fun onSuccess(songs: List<SongListItem>)
}
