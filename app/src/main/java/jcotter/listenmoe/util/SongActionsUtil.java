package jcotter.listenmoe.util;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import jcotter.listenmoe.R;
import jcotter.listenmoe.constants.ResponseMessages;
import jcotter.listenmoe.interfaces.FavoriteSongListener;
import jcotter.listenmoe.interfaces.RequestSongListener;
import jcotter.listenmoe.model.Song;

public class SongActionsUtil {

    /**
     * Updates the favorite status of a song.
     *
     * @param song The song to update the favorite status of.
     */
    public static void favorite(final Activity activity, final RecyclerView.Adapter adapter, final Song song) {
        APIUtil.favoriteSong(activity, song.getId(), new FavoriteSongListener() {
            @Override
            public void onFailure(final String result) {
                activity.runOnUiThread(() -> Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onSuccess(final boolean favorited) {
                activity.runOnUiThread(() -> {
                    song.setFavorite(favorited);
                    adapter.notifyDataSetChanged();
                });
            }
        });
    }

    /**
     * Requests a song.
     *
     * @param song The song to request.
     */
    public static void request(final Activity activity, final RecyclerView.Adapter adapter, final Song song) {
        APIUtil.requestSong(activity, song.getId(), new RequestSongListener() {
            @Override
            public void onFailure(final String result) {
                activity.runOnUiThread(() -> {
                    if (result.equals(ResponseMessages.USER_NOT_SUPPORTER)) {
                        Toast.makeText(activity, R.string.supporter_required, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(activity, R.string.error, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onSuccess() {
                activity.runOnUiThread(() -> {
                    Toast.makeText(activity, R.string.success, Toast.LENGTH_LONG).show();

                    song.setEnabled(false);
                    adapter.notifyDataSetChanged();
                });
            }
        });
    }
}
