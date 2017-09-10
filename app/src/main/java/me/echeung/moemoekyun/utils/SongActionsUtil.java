package me.echeung.moemoekyun.utils;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.api.APIClient;
import me.echeung.moemoekyun.api.ResponseMessages;
import me.echeung.moemoekyun.api.interfaces.FavoriteSongListener;
import me.echeung.moemoekyun.api.interfaces.RequestSongListener;
import me.echeung.moemoekyun.api.models.Song;
import me.echeung.moemoekyun.ui.App;
import me.echeung.moemoekyun.ui.fragments.UserFragment;

public class SongActionsUtil {

    /**
     * Updates the favorite status of a song.
     *
     * @param song The song to update the favorite status of.
     */
    public static void favorite(final Activity activity, final RecyclerView.Adapter adapter, final Song song) {
        final int songId = song.getId();
        APIClient.favoriteSong(activity, songId, new FavoriteSongListener() {
            @Override
            public void onFailure(final String result) {
                if (activity != null) {
                    activity.runOnUiThread(() -> Toast.makeText(activity.getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onSuccess(final boolean favorited) {
                if (App.getRadioViewModel().getCurrentSong().getId() == songId) {
                    App.getRadioViewModel().setIsFavorited(favorited);
                }

                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        song.setFavorite(favorited);
                        adapter.notifyDataSetChanged();

                        // Broadcast change
                        final Intent favIntent = new Intent(UserFragment.FAVORITE_EVENT);
                        activity.sendBroadcast(favIntent);

                        // Undo action
                        if (!favorited) {
                            final View coordinatorLayout = activity.findViewById(R.id.coordinator_layout);
                            if (coordinatorLayout != null) {
                                final Snackbar undoBar = Snackbar.make(coordinatorLayout,
                                        String.format(activity.getString(R.string.unfavorited), song.getTitle()),
                                        Snackbar.LENGTH_LONG);
                                undoBar.setAction(R.string.action_undo, (v) -> favorite(activity, adapter, song));
                                undoBar.show();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Requests a song.
     *
     * @param song The song to request.
     */
    public static void request(final Activity activity, final RecyclerView.Adapter adapter, final Song song) {
        APIClient.requestSong(activity, song.getId(), new RequestSongListener() {
            @Override
            public void onFailure(final String result) {
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        final int message = result.equals(ResponseMessages.USER_NOT_SUPPORTER) ?
                                R.string.supporter_required :
                                R.string.error;

                        Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    });
                }
            }

            @Override
            public void onSuccess() {
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        Toast.makeText(activity.getApplicationContext(), R.string.success, Toast.LENGTH_LONG).show();

                        song.setEnabled(false);
                        adapter.notifyDataSetChanged();
                    });
                }
            }
        });
    }
}
