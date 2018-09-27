package me.echeung.moemoekyun.client.api.callback

import me.echeung.moemoekyun.client.model.ArtistSummary

interface ArtistsCallback : BaseCallback {
    fun onSuccess(artists: List<ArtistSummary>)
}
