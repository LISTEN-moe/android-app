package me.echeung.moemoekyun.client.api

import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChartsApiClient @Inject constructor(private val api: ListenMoeApi) {

    suspend fun songs(
        station: String,
        window: String = DEFAULT_WINDOW,
        mode: String = DEFAULT_MODE,
    ): ChartSongsResponse = api.chartSongs(station, window, mode)

    suspend fun artists(
        station: String,
        window: String = DEFAULT_WINDOW,
        mode: String = DEFAULT_MODE,
    ): ChartEntitiesResponse = api.chartArtists(station, window, mode)

    suspend fun albums(
        station: String,
        window: String = DEFAULT_WINDOW,
        mode: String = DEFAULT_MODE,
    ): ChartEntitiesResponse = api.chartAlbums(station, window, mode)

    companion object {
        const val DEFAULT_WINDOW = "24h"
        const val DEFAULT_MODE = "plays"
    }
}

@Serializable
data class ChartSongsResponse(val entries: List<ChartSongEntry> = emptyList())

@Serializable
data class ChartSongEntry(val rank: Int, val count: Int, val entity: ChartSongEntity)

@Serializable
data class ChartSongEntity(
    val id: Int,
    val title: String? = null,
    val titleRomaji: String? = null,
    val artists: List<ChartDescriptor> = emptyList(),
    val albums: List<ChartDescriptor> = emptyList(),
)

@Serializable
data class ChartEntitiesResponse(val entries: List<ChartEntityEntry> = emptyList())

@Serializable
data class ChartEntityEntry(val rank: Int, val count: Int, val entity: ChartNamedEntity)

@Serializable
data class ChartNamedEntity(
    val id: Int,
    val name: String? = null,
    val nameRomaji: String? = null,
    val image: String? = null,
)

@Serializable
data class ChartDescriptor(val name: String? = null, val nameRomaji: String? = null, val image: String? = null)
