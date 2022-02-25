package me.echeung.moemoekyun.client.api.data

import me.echeung.moemoekyun.client.api.model.Song
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * A naive in-memory cache of the songs list for faster loading/searching.
 */
class SongsCache(private val songsProvider: suspend () -> List<Song>) {

    private var cachedSongs: List<Song>? = null
    private var lastUpdated = 0L

    private val isCacheValid: Boolean
        get() = Date().time - lastUpdated < MAX_AGE

    suspend fun getSongs(): List<Song>? {
        if (lastUpdated != 0L && isCacheValid && cachedSongs != null) {
            return cachedSongs
        }

        lastUpdated = Date().time
        cachedSongs = songsProvider()

        return cachedSongs
    }
}

private val MAX_AGE = TimeUnit.DAYS.toMillis(1)
