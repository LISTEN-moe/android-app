package me.echeung.moemoekyun.client.api.cache

import android.util.Log
import me.echeung.moemoekyun.client.api.APIClient
import me.echeung.moemoekyun.client.api.callback.SongsCallback
import me.echeung.moemoekyun.client.model.SongListItem
import java.util.GregorianCalendar

/**
 * A naive cache of the songs data from the API for faster loading/searching.
 */
class SongsCache(private val apiClient: APIClient) {

    private var cachedSongs: List<SongListItem>? = null
    private var lastUpdated = 0L

    private val isCacheValid: Boolean
        get() = GregorianCalendar().timeInMillis - lastUpdated < MAX_AGE

    init {
        // Prime the cache
        getSongs(null)
    }

    fun getSongs(callback: Callback?) {
        if (lastUpdated != 0L && isCacheValid && cachedSongs != null && callback != null) {
            callback.onRetrieve(cachedSongs)
        }

        apiClient.getSongs(object : SongsCallback {
            override fun onSuccess(songs: List<SongListItem>) {
                lastUpdated = GregorianCalendar().timeInMillis
                cachedSongs = songs

                callback?.onRetrieve(cachedSongs)
            }

            override fun onFailure(message: String?) {
                Log.e(TAG, message)
                callback?.onFailure(message)
            }
        })
    }

    interface Callback {
        fun onRetrieve(songs: List<SongListItem>?)

        fun onFailure(message: String?)
    }

    companion object {
        private val TAG = SongsCache::class.java.simpleName

        private const val MAX_AGE = 1000 * 60 * 60 * 24 // 24 hours
    }
}
