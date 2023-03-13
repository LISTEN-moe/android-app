package me.echeung.moemoekyun.domain.songs.interactor

import me.echeung.moemoekyun.domain.songs.SongsService
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import javax.inject.Inject

class GetSong @Inject constructor(
    private val songsService: SongsService,
    private val getFavoriteSongs: GetFavoriteSongs,
) {

    suspend fun await(songId: Int): DomainSong {
        val userFavorites = getFavoriteSongs.getAll().map { it.id }
        return songsService.getDetailedSong(songId)
            .let { it.copy(favorited = it.id in userFavorites) }
    }
}
