package me.echeung.listenmoeapi.cache;

import android.util.Log;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import me.echeung.listenmoeapi.APIClient;
import me.echeung.listenmoeapi.callbacks.SongsCallback;
import me.echeung.listenmoeapi.models.SongListItem;

/**
 * A naive cache of the songs data from the API for faster loading/searching.
 */
public class SongsCache {

    private static String TAG = SongsCache.class.getSimpleName();

    private static final int MAX_AGE = 1000 * 60 * 60 * 24;

    private APIClient apiClient;

    private List<SongListItem> cachedSongs;
    private long lastUpdated = 0L;

    public SongsCache(APIClient apiClient) {
        this.apiClient = apiClient;
    }

    public void getSongs(Callback callback) {
        if (lastUpdated != 0L && isCacheValid() && cachedSongs != null) {
            callback.onRetrieve(cachedSongs);
        }

        apiClient.getSongs(new SongsCallback() {
            @Override
            public void onSuccess(List<SongListItem> songs) {
                lastUpdated = new GregorianCalendar().getTimeInMillis();
                cachedSongs = songs;

                callback.onRetrieve(cachedSongs);
            }

            @Override
            public void onFailure(String message) {
                Log.e(TAG, message);
                callback.onFailure(message);
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
