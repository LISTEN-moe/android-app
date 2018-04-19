package me.echeung.moemoekyun.api.cache;

import android.util.Log;

import java.util.GregorianCalendar;
import java.util.List;

import me.echeung.moemoekyun.api.callbacks.SongsCallback;
import me.echeung.moemoekyun.api.clients.APIClient;
import me.echeung.moemoekyun.model.SongListItem;

/**
 * A naive cache of the songs data from the API for faster loading/searching.
 */
public class SongsCache {

    private static String TAG = SongsCache.class.getSimpleName();

    private static final int MAX_AGE = 1000 * 60 * 60 * 24;  // 24 hours

    private APIClient apiClient;

    private List<SongListItem> cachedSongs;
    private long lastUpdated = 0L;

    public SongsCache(APIClient apiClient) {
        this.apiClient = apiClient;

        // Prime the cache
        getSongs(null);
    }

    public void getSongs(Callback callback) {
        if (lastUpdated != 0L && isCacheValid() && cachedSongs != null && callback != null) {
            callback.onRetrieve(cachedSongs);
        }

        apiClient.getSongs(new SongsCallback() {
            @Override
            public void onSuccess(List<SongListItem> songs) {
                lastUpdated = new GregorianCalendar().getTimeInMillis();
                cachedSongs = songs;

                if (callback != null) {
                    callback.onRetrieve(cachedSongs);
                }
            }

            @Override
            public void onFailure(String message) {
                Log.e(TAG, message);
                if (callback != null) {
                    callback.onFailure(message);
                }
            }
        });
    }

    private boolean isCacheValid() {
        return new GregorianCalendar().getTimeInMillis() - lastUpdated < MAX_AGE;
    }

    public interface Callback {
        void onRetrieve(List<SongListItem> songs);
        void onFailure(String message);
    }

}
