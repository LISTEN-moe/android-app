package me.echeung.moemoekyun.client.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChartsApiClient @Inject constructor(private val okHttpClient: OkHttpClient, private val json: Json) {

    suspend fun songs(
        station: String,
        window: String = DEFAULT_WINDOW,
        mode: String = DEFAULT_MODE,
    ): ChartSongsResponse {
        val body = get("songs", station, window, mode)
        return json.decodeFromString<ChartSongsResponse>(body)
    }

    suspend fun artists(
        station: String,
        window: String = DEFAULT_WINDOW,
        mode: String = DEFAULT_MODE,
    ): ChartEntitiesResponse {
        val body = get("artists", station, window, mode)
        return json.decodeFromString<ChartEntitiesResponse>(body)
    }

    suspend fun albums(
        station: String,
        window: String = DEFAULT_WINDOW,
        mode: String = DEFAULT_MODE,
    ): ChartEntitiesResponse {
        val body = get("albums", station, window, mode)
        return json.decodeFromString<ChartEntitiesResponse>(body)
    }

    private fun get(entityType: String, station: String, window: String, mode: String): String {
        val url = "https://listen.moe/api/v1/charts/$entityType".toHttpUrl().newBuilder()
            .addQueryParameter("station", station)
            .addQueryParameter("window", window)
            .addQueryParameter("mode", mode)
            .build()

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        return okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Charts request failed: ${response.code}")
            response.body.string()
        }
    }

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
