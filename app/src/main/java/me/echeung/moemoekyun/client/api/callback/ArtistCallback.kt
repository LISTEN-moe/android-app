package me.echeung.moemoekyun.client.api.callback

import me.echeung.moemoekyun.client.model.Artist

interface ArtistCallback : BaseCallback {
    fun onSuccess(artist: Artist)
}
