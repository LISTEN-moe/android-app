package me.echeung.moemoekyun.util

import me.echeung.moemoekyun.client.api.model.Song
import me.echeung.moemoekyun.client.api.model.getSongDisplayString

class SongFormatter(
    private val preferenceUtil: PreferenceUtil,
) {

    fun getTitle(song: Song?) =
        if (preferenceUtil.shouldPreferRomaji().get() && !song?.titleRomaji.isNullOrBlank()) {
            song?.titleRomaji
        } else {
            song?.title
        } ?: FALLBACK

    fun getArtists(song: Song?) = song?.artists.getSongDisplayString(preferenceUtil.shouldPreferRomaji().get()) ?: FALLBACK

    fun getAlbums(song: Song?) = song?.albums.getSongDisplayString(preferenceUtil.shouldPreferRomaji().get()) ?: FALLBACK

    fun getSources(song: Song?) = song?.sources.getSongDisplayString(preferenceUtil.shouldPreferRomaji().get()) ?: FALLBACK
}

private const val FALLBACK = "–"
