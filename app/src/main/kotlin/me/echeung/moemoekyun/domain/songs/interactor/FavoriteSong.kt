package me.echeung.moemoekyun.domain.songs.interactor

import me.echeung.moemoekyun.domain.songs.SongsService
import javax.inject.Inject

class FavoriteSong @Inject constructor(
    private val songsService: SongsService,
) {

    suspend fun await(songId: Int): Boolean {
        return songsService.favorite(songId).favorited
    }
}
