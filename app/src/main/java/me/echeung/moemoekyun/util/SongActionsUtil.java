package me.echeung.moemoekyun.util;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.adapter.SongDetailAdapter;
import me.echeung.moemoekyun.client.api.callback.FavoriteSongCallback;
import me.echeung.moemoekyun.client.api.callback.RequestSongCallback;
import me.echeung.moemoekyun.client.model.Song;
import me.echeung.moemoekyun.client.model.User;

public final class SongActionsUtil {

    public static final String REQUEST_EVENT = "req_event";
    public static final String FAVORITE_EVENT = "fav_event";

    public static void showSongsDialog(Activity activity, String title, Song song) {
        List<Song> songList = new ArrayList<>();
        songList.add(song);

        showSongsDialog(activity, title, songList);
    }

    public static void showSongsDialog(Activity activity, String title, List<Song> songs) {
        if (activity == null) return;

        new AlertDialog.Builder(activity, R.style.DialogTheme)
                .setTitle(title)
                .setAdapter(new SongDetailAdapter(activity, songs), null)
                .setPositiveButton(R.string.close, null)
                .create()
                .show();
    }

    /**
     * Updates the favorite status of a song.
     *
     * @param song The song to update the favorite status of.
     */
    public static void toggleFavorite(Activity activity, Song song) {
        int songId = song.getId();
        boolean isCurrentlyFavorite = song.isFavorite();

        FavoriteSongCallback callback = new FavoriteSongCallback() {
            @Override
            public void onSuccess() {
                if (App.getRadioViewModel().getCurrentSong().getId() == songId) {
                    App.getRadioViewModel().setIsFavorited(!isCurrentlyFavorite);
                }
                song.setFavorite(!isCurrentlyFavorite);

                if (activity == null) return;

                activity.runOnUiThread(() -> {
                    // Broadcast event
                    activity.sendBroadcast(new Intent(SongActionsUtil.FAVORITE_EVENT));

                    if (isCurrentlyFavorite) {
                        // Undo action
                        View coordinatorLayout = activity.findViewById(R.id.coordinator_layout);
                        if (coordinatorLayout != null) {
                            Snackbar undoBar = Snackbar.make(coordinatorLayout,
                                    String.format(activity.getString(R.string.unfavorited), song.toString()),
                                    Snackbar.LENGTH_LONG);
                            undoBar.setAction(R.string.action_undo, v -> toggleFavorite(activity, song));
                            undoBar.show();
                        }
                    }
                });
            }

            @Override
            public void onFailure(String message) {
                if (activity == null) return;

                activity.runOnUiThread(() -> Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_SHORT).show());
            }
        };

        App.getRadioClient().getApi().toggleFavorite(String.valueOf(songId), isCurrentlyFavorite, callback);
    }

    /**
     * Requests a song.
     *
     * @param song The song to request.
     */
    public static void request(Activity activity, Song song) {
        User user = App.getUserViewModel().getUser();
        if (user == null) {
            return;
        }

        App.getRadioClient().getApi().requestSong(String.valueOf(song.getId()), new RequestSongCallback() {
            @Override
            public void onSuccess() {
                if (activity == null) return;

                activity.runOnUiThread(() -> {
                    // Broadcast event
                    activity.sendBroadcast(new Intent(SongActionsUtil.REQUEST_EVENT));

                    // Instantly update remaining requests number to appear responsive
                    int remainingRequests = user.getRequestsRemaining() - 1;
                    App.getUserViewModel().setRequestsRemaining(remainingRequests);

                    String toastMsg = App.getPreferenceUtil().shouldShowRandomRequestTitle()
                            ? activity.getString(R.string.requested_song, song.toString())
                            : activity.getString(R.string.requested_random_song);

                    Toast.makeText(activity.getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onFailure(String message) {
                if (activity == null) return;

                activity.runOnUiThread(() -> Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    public static void copyToClipboard(Context context, Song song) {
        if (context == null || song == null) return;

        copyToClipboard(context, song.toString());
    }

    public static void copyToClipboard(Context context, String songInfo) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("song", songInfo);
        clipboard.setPrimaryClip(clip);

        String text = String.format("%s: %s", context.getString(R.string.copied_to_clipboard), songInfo);

        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

}
