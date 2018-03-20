package me.echeung.moemoekyun.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import me.echeung.listenmoeapi.callbacks.FavoriteSongCallback;
import me.echeung.listenmoeapi.callbacks.RequestSongCallback;
import me.echeung.listenmoeapi.models.Song;
import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.adapters.songslist.SongAdapter;

public final class SongActionsUtil {

    public static final String REQUEST_EVENT = "req_event";
    public static final String FAVORITE_EVENT = "fav_event";

    public static void showSongActionsDialog(final Activity activity, final SongAdapter adapter, final Song song) {
        if (activity == null) return;

        final String favoriteAction = song.isFavorite() ?
                activity.getString(R.string.action_unfavorite) :
                activity.getString(R.string.action_favorite);

        new AlertDialog.Builder(activity, R.style.DialogTheme)
                .setTitle(song.getTitle())
                .setMessage(song.getArtistsString() + "\n" + song.getAlbumsString())
                .setPositiveButton(android.R.string.cancel, null)
                .setNegativeButton(favoriteAction, (dialogInterface, in) -> SongActionsUtil.toggleFavorite(activity, adapter, song))
                .setNeutralButton(activity.getString(R.string.action_request), (dialogInterface, im) -> SongActionsUtil.request(activity, adapter, song))
                .create()
                .show();
    }

    /**
     * Updates the favorite status of a song.
     *
     * @param song The song to update the favorite status of.
     */
    public static void toggleFavorite(final Activity activity, final RecyclerView.Adapter adapter, final Song song) {
        final int songId = song.getId();
        final boolean isCurrentlyFavorite = song.isFavorite();

        final FavoriteSongCallback callback = new FavoriteSongCallback() {
            @Override
            public void onSuccess() {
                if (App.getRadioViewModel().getCurrentSong().getId() == songId) {
                    App.getRadioViewModel().setIsFavorited(!isCurrentlyFavorite);
                }

                if (activity == null) return;

                activity.runOnUiThread(() -> {
                    song.setFavorite(!isCurrentlyFavorite);
                    adapter.notifyDataSetChanged();

                    // Broadcast event
                    final Intent favIntent = new Intent(SongActionsUtil.FAVORITE_EVENT);
                    activity.sendBroadcast(favIntent);

                    if (isCurrentlyFavorite) {
                        // Undo action
                        final View coordinatorLayout = activity.findViewById(R.id.coordinator_layout);
                        if (coordinatorLayout != null) {
                            final Snackbar undoBar = Snackbar.make(coordinatorLayout,
                                    String.format(activity.getString(R.string.unfavorited), song.toString()),
                                    Snackbar.LENGTH_LONG);
                            undoBar.setAction(R.string.action_undo, (v) -> toggleFavorite(activity, adapter, song));
                            undoBar.show();
                        }
                    }
                });
            }

            @Override
            public void onFailure(final String message) {
                if (activity == null) return;

                activity.runOnUiThread(() -> Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_SHORT).show());
            }
        };

        App.getApiClient().toggleFavorite(String.valueOf(songId), isCurrentlyFavorite, callback);
    }

    /**
     * Requests a song.
     *
     * @param song The song to request.
     */
    public static void request(final Activity activity, final RecyclerView.Adapter adapter, final Song song) {
        final int requests = App.getUserViewModel().getUser().getRequestsRemaining();
        App.getApiClient().requestSong(String.valueOf(song.getId()), new RequestSongCallback() {
            @Override
            public void onSuccess() {
                if (activity == null) return;

                activity.runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();

                    // Broadcast event
                    final Intent reqEvent = new Intent(SongActionsUtil.REQUEST_EVENT);
                    activity.sendBroadcast(reqEvent);

                    // Instantly update remaining requests number to appear responsive
                    final int remainingRequests = requests - 1;
                    App.getUserViewModel().setRequestsRemaining(remainingRequests);

                    final String toastMsg = App.getPreferenceUtil().shouldShowRandomRequestTitle()
                            ? activity.getString(R.string.requested_song, song.toString())
                            : activity.getString(R.string.requested_random_song);

                    Toast.makeText(activity.getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onFailure(final String message) {
                if (activity == null) return;

                activity.runOnUiThread(() -> Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    public static void copyToClipboard(final Context context, final Song song) {
        if (context == null) return;

        copyToClipboard(context, song.toString());
    }

    public static void copyToClipboard(final Context context, final String songInfo) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("song", songInfo);
        clipboard.setPrimaryClip(clip);

        String text = String.format("%s: %s", context.getString(R.string.copied_to_clipboard), songInfo);

        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

}
