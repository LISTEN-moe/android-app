package me.echeung.moemoekyun.client.api.data

import me.echeung.moemoekyun.FavoritesQuery
import me.echeung.moemoekyun.SongQuery
import me.echeung.moemoekyun.SongsQuery
import me.echeung.moemoekyun.UserQuery
import me.echeung.moemoekyun.client.api.model.Song
import me.echeung.moemoekyun.client.api.model.SongDescriptor
import me.echeung.moemoekyun.client.api.model.User
import me.echeung.moemoekyun.fragment.SongFields
import me.echeung.moemoekyun.fragment.SongListFields

fun UserQuery.User.transform() = User(
    this.uuid,
    this.displayName!!,
    this.avatarImage,
    this.bannerImage,
)

fun FavoritesQuery.Song.transform() = fragments.songListFields.transform().apply {
    // Manually mark a user's favorite as favorited
    favorite = true
}

fun SongQuery.Song.transform() = fragments.songFields.transform()

fun SongsQuery.Song.transform() = fragments.songListFields.transform()

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
