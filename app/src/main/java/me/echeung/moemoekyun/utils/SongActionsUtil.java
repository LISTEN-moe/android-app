package me.echeung.moemoekyun.utils;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.api.old.APIUtil;
import me.echeung.moemoekyun.api.old.interfaces.FavoriteSongListener;
import me.echeung.moemoekyun.api.old.interfaces.RequestSongListener;
import me.echeung.moemoekyun.api.old.model.Song;
import me.echeung.moemoekyun.constants.ResponseMessages;
import me.echeung.moemoekyun.state.AppState;
import me.echeung.moemoekyun.ui.fragments.UserFragment;

public class SongActionsUtil {

    /**
     * Updates the favorite status of a song.
     *
     * @param song The song to update the favorite status of.
     */
    public static void favorite(final Activity activity, final RecyclerView.Adapter adapter, final Song song) {
        final int songId = song.getId();
        APIUtil.favoriteSong(activity, songId, new FavoriteSongListener() {
            @Override
            public void onFailure(final String result) {
                activity.runOnUiThread(() -> Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onSuccess(final boolean favorited) {
                if (AppState.getInstance().currentSong.get().getId() == songId) {
                    AppState.getInstance().setFavorited(favorited);
                }

                activity.runOnUiThread(() -> {
                    song.setFavorite(favorited);
                    adapter.notifyDataSetChanged();

                    // Broadcast change
                    final Intent favIntent = new Intent(UserFragment.FAVORITE_EVENT);
                    activity.sendBroadcast(favIntent);

                    // Undo action
                    if (!favorited) {
                        final Snackbar undoBar = Snackbar.make(activity.findViewById(R.id.coordinator_layout),
                                String.format(activity.getString(R.string.unfavorited), song.getTitle()),
                                Snackbar.LENGTH_LONG);
                        undoBar.setAction(R.string.action_undo, (v) -> favorite(activity, adapter, song));
                        undoBar.show();
                    }
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
