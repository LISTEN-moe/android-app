package me.echeung.moemoekyun.data.database

import me.echeung.moemoekyun.domain.songs.model.DomainSong

/** Flat result of the songs ⨯ favourites JOIN, mapped directly by Room. */
data class FavouriteSongView(
    val id: Int,
    val title: String,
    val artists: String?,
    val albums: String?,
    val sources: String?,
    val duration: String,
    val durationSeconds: Long,
    val albumArtUrl: String?,
    val favoritedAtEpoch: Long?,
)

fun FavouriteSongView.toDomainSong() = DomainSong(
    id = id,
    title = title,
    artists = artists,
    albums = albums,
    sources = sources,
    duration = duration,
    durationSeconds = durationSeconds,
    albumArtUrl = albumArtUrl,
    favorited = true,
    favoritedAtEpoch = favoritedAtEpoch,
)
