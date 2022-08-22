package me.echeung.moemoekyun.client.api.data

import me.echeung.moemoekyun.FavoritesQuery
import me.echeung.moemoekyun.SongQuery
import me.echeung.moemoekyun.SongsQuery
import me.echeung.moemoekyun.UserQuery
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.client.model.SongDescriptor
import me.echeung.moemoekyun.client.model.User
import me.echeung.moemoekyun.fragment.SongFields
import me.echeung.moemoekyun.fragment.SongListFields

fun UserQuery.User.transform() = User(
    this.uuid,
    this.displayName!!,
    this.avatarImage,
    this.bannerImage,
)

fun FavoritesQuery.Song.transform(): Song {
    val song = songListFields.transform()

    // Manually mark a user's favorite as favorited
    song.favorite = true

    return song
}

fun SongQuery.Song.transform() = songFields.transform()

fun SongsQuery.Song.transform() = songListFields.transform()

private fun SongFields.transform() = Song(
    this.id,
    this.title,
    this.titleRomaji,
    this.artists.mapNotNull { it?.transform() },
    this.sources.mapNotNull { it?.transform() },
    this.albums.mapNotNull { it?.transform() },
    this.duration,
)

private fun SongFields.Artist.transform() = SongDescriptor(
    this.name,
    this.nameRomaji,
    this.image,
)

private fun SongFields.Source.transform() = SongDescriptor(
    this.name,
    this.nameRomaji,
    this.image,
)

private fun SongFields.Album.transform() = SongDescriptor(
    this.name,
    this.nameRomaji,
    this.image,
)

private fun SongListFields.transform() = Song(
    this.id,
    this.title,
    this.titleRomaji,
    this.artists.mapNotNull { it?.transform() },
)

private fun SongListFields.Artist.transform() = SongDescriptor(
    this.name,
    this.nameRomaji,
)
