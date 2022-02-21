package me.echeung.moemoekyun.client.api.data

import me.echeung.moemoekyun.client.api.APIClient
import me.echeung.moemoekyun.client.api.model.Song
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * A naive cache of the songs data from the API for faster loading/searching.
 */
class SongsCache(private val apiClient: APIClient) {

    private var cachedSongs: List<Song>? = null
    private var lastUpdated = 0L

    private val isCacheValid: Boolean
        get() = Date().time - lastUpdated < MAX_AGE

    suspend fun getSongs(): List<Song>? {
        if (lastUpdated != 0L && isCacheValid && cachedSongs != null) {
            return cachedSongs
        }

        val songs = apiClient.getAllSongs()
        lastUpdated = Date().time
        cachedSongs = songs

        return cachedSongs
    }
}

private val MAX_AGE = TimeUnit.DAYS.toMillis(1)
