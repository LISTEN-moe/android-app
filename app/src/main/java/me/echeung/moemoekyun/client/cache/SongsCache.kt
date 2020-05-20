package me.echeung.moemoekyun.client.cache

import java.util.GregorianCalendar
import me.echeung.moemoekyun.client.api.APIClient
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.util.system.launchIO

/**
 * A naive cache of the songs data from the API for faster loading/searching.
 */
class SongsCache(private val apiClient: APIClient) {

    private var cachedSongs: List<Song>? = null
    private var lastUpdated = 0L

    private val isCacheValid: Boolean
        get() = GregorianCalendar().timeInMillis - lastUpdated < MAX_AGE

    init {
        // Prime the cache
        launchIO { getSongs() }
    }

    suspend fun getSongs(): List<Song>? {
        if (lastUpdated != 0L && isCacheValid && cachedSongs != null) {
            return cachedSongs
        }

        val songs = apiClient.getAllSongs()
        lastUpdated = GregorianCalendar().timeInMillis
        cachedSongs = songs

        return cachedSongs
    }

    companion object {
        private const val MAX_AGE = 1000 * 60 * 60 * 24 // 24 hours
    }
}
