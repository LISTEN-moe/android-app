package me.echeung.moemoekyun.domain.songs

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.echeung.moemoekyun.client.api.ApiClient
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.domain.songs.model.SongConverter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongsService @Inject constructor(private val api: ApiClient, private val songConverter: SongConverter) {

    private val _favoriteEvents = MutableStateFlow<DomainSong?>(null)
    val favoriteEvents = _favoriteEvents.asStateFlow()

    private val cache = mutableMapOf<Int, Song>()

    suspend fun getDetailedSong(songId: Int): DomainSong {
        val cached = cache[songId]
        if (cached != null && !cached.albums.isNullOrEmpty()) {
            return songConverter.toDomainSong(cached)
        }

        val detailedSong = api.getSongDetails(songId)
        cache[songId] = detailedSong
        return songConverter.toDomainSong(detailedSong)
    }

    suspend fun favorite(songId: Int): DomainSong {
        api.toggleFavorite(songId)

        val song = getDetailedSong(songId)

        val newState = !song.favorited
        val updatedSong = song.copy(
            favorited = newState,
            favoritedAtEpoch = if (newState) System.currentTimeMillis() else null,
        )

        cache[songId]?.let {
            cache[songId] = it.copy(
                favorite = newState,
                favoritedAt = if (newState) System.currentTimeMillis() else null,
            )
        }

        _favoriteEvents.emit(updatedSong)

        return updatedSong
    }

    suspend fun request(songId: Int) {
        api.requestSong(songId)
    }
}
