package me.echeung.moemoekyun.client.api.data

import me.echeung.moemoekyun.client.api.APIClient
import me.echeung.moemoekyun.client.model.Song
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * A naive cache of the songs data from the API for faster loading/searching.
 */
class SongsCache(private val apiClient: APIClient) {

    private var cachedSongs: List<Song> = emptyList()
    private var lastUpdated = 0L

    suspend fun getSongs(): List<Song> {
        if (lastUpdated != 0L && isCacheValid() && cachedSongs.isNotEmpty()) {
            return cachedSongs
        }

        val songs = apiClient.getAllSongs()
        lastUpdated = Date().time
        cachedSongs = songs

        return cachedSongs
    }

    private fun isCacheValid() = Date().time - lastUpdated < MAX_AGE
}

private val MAX_AGE = TimeUnit.DAYS.toMillis(1)
