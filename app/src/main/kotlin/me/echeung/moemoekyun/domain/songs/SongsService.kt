package me.echeung.moemoekyun.domain.songs

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import me.echeung.moemoekyun.client.api.ApiClient
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.domain.songs.model.SongConverter
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongsService @Inject constructor(private val api: ApiClient, private val songConverter: SongConverter) {

    private val _songs = MutableStateFlow<Map<Int, DomainSong>>(emptyMap())
    val songs = _songs.asStateFlow().onStart { getSongs() }

    private val _favoriteEvents = MutableStateFlow<DomainSong?>(null)
    val favoriteEvents = _favoriteEvents.asStateFlow()

    private var lastUpdated = 0L

    suspend fun getDetailedSong(songId: Int): DomainSong {
        val song = _songs.value[songId]

        if (song != null && !song.albums.isNullOrEmpty()) {
            return song
        }

        val detailedSong = api.getSongDetails(songId).let(songConverter::toDomainSong)

        val updatedMap = _songs.value.toMutableMap()
        updatedMap[songId] = detailedSong
        _songs.value = updatedMap

        return detailedSong
    }

    suspend fun favorite(songId: Int): DomainSong {
        api.toggleFavorite(songId)

        val song = getDetailedSong(songId)

        val newState = !song.favorited
        val updatedSong = song.copy(
            favorited = newState,
            favoritedAtEpoch = if (newState) System.currentTimeMillis() else null,
        )

        val updatedMap = _songs.value.toMutableMap()
        updatedMap[songId] = updatedSong
        _songs.value = updatedMap

        _favoriteEvents.emit(updatedSong)

        return updatedSong
    }

    suspend fun request(songId: Int) {
        api.requestSong(songId)
    }

    private suspend fun getSongs(): List<DomainSong> {
        if (lastUpdated == 0L || !isCacheValid() || _songs.value.isEmpty()) {
            val songs = api.getAllSongs().map(songConverter::toDomainSong)
            lastUpdated = Date().time
            _songs.value = songs.associateBy { it.id }.toMutableMap()
        }

        return _songs.value.values.toList()
    }

    private fun isCacheValid() = Date().time - lastUpdated < MAX_AGE
}

private val MAX_AGE = TimeUnit.DAYS.toMillis(1)
