package me.echeung.moemoekyun.client.api.data

import me.echeung.moemoekyun.FavoritesQuery
import me.echeung.moemoekyun.SongQuery
import me.echeung.moemoekyun.SongsQuery
import me.echeung.moemoekyun.UserQuery
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.client.model.SongDescriptor
import me.echeung.moemoekyun.client.model.User
import me.echeung.moemoekyun.fragment.SongFields

fun UserQuery.User.transform(): User {
    return User(
            this.uuid,
            this.displayName!!,
            this.avatarImage,
            this.bannerImage
    )
}

fun FavoritesQuery.Song.transform(): Song {
    val song = this.fragments.songFields.transform()

    // Manually mark a user's favorite as favorited
    song.favorite = true

    return song
}

fun SongQuery.Song.transform(): Song {
    return this.fragments.songFields.transform()
}

fun SongsQuery.Song1.transform(): Song {
    return this.fragments.songFields.transform()
}

private fun SongFields.transform(): Song {
    return Song(
            this.id,
            this.title,
            this.titleRomaji,
            this.artists.mapNotNull { it?.transform() },
            this.sources.mapNotNull { it?.transform() },
            this.albums.mapNotNull { it?.transform() },
            this.duration
    )
}

private fun SongFields.Artist.transform(): SongDescriptor {
    return SongDescriptor(
            this.id,
            this.name,
            this.nameRomaji,
            this.image
    )
}

private fun SongFields.Source.transform(): SongDescriptor {
    return SongDescriptor(
            this.id,
            this.name,
            this.nameRomaji,
            this.image
    )
}

private fun SongFields.Album.transform(): SongDescriptor {
    return SongDescriptor(
            this.id,
            this.name,
            this.nameRomaji,
            this.image
    )
}
