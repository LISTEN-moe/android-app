package me.echeung.moemoekyun.client.api.v5

import me.echeung.moemoekyun.FavoritesQuery
import me.echeung.moemoekyun.SearchQuery
import me.echeung.moemoekyun.UserQuery
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.client.model.User

fun UserQuery.User.transform(): User {
    return User(
            this.displayName!!,
            this.avatarImage,
            this.bannerImage,
            this.additionalSongRequests
    )
}

fun FavoritesQuery.Favorite.transform(): List<Song> {
    if (this.favorites.isNullOrEmpty()) {
        return emptyList()
    }

    return this.favorites
            .mapNotNull { it?.song }
            .map { it.transform() }
}

fun FavoritesQuery.Song.transform(): Song {
    return Song(
            this.fragments.songFields.id,
            this.fragments.songFields.title,
            this.fragments.songFields.titleRomaji,
            this.fragments.songFields.titleSearchRomaji,
            emptyList(),
            emptyList(),
            emptyList(),
            // TODO
//            this.fragments.songFields.artists,
//            this.fragments.songFields.sources,
//            this.fragments.songFields.albums,
            this.fragments.songFields.duration
    )
}

fun SearchQuery.Search.transform(): Song {
    return Song(
            this.fragments.songFields!!.id,
            this.fragments.songFields.title,
            this.fragments.songFields.titleRomaji,
            this.fragments.songFields.titleSearchRomaji,
            emptyList(),
            emptyList(),
            emptyList(),
            // TODO
//            this.fragments.songFields.artists,
//            this.fragments.songFields.sources,
//            this.fragments.songFields.albums,
            this.fragments.songFields.duration
    )
}
