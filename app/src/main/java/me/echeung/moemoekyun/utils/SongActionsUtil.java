package me.echeung.moemoekyun.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
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
import me.echeung.moemoekyun.adapters.SongAdapter;
import me.echeung.moemoekyun.ui.fragments.UserFragment;

public final class SongActionsUtil {

    public static void showSongActionsDialog(final Activity activity, final SongAdapter adapter, final Song song) {
        final String favoriteAction = song.isFavorite() ?
                activity.getString(R.string.action_unfavorite) :
                activity.getString(R.string.action_favorite);

        final DialogInterface.OnClickListener favoriteActionListener = song.isFavorite() ?
                (dialogInterface, in) -> SongActionsUtil.unfavorite(activity, adapter, song) :
                (dialogInterface, in) -> SongActionsUtil.favorite(activity, adapter, song);

        new AlertDialog.Builder(activity, R.style.DialogTheme)
                .setTitle(song.getTitle())
                .setMessage(song.getArtistString() + "\n" + song.getAlbumString())
                .setPositiveButton(android.R.string.cancel, null)
                .setNegativeButton(favoriteAction, favoriteActionListener)
                .setNeutralButton(activity.getString(R.string.action_request), (dialogInterface, im) -> SongActionsUtil.request(activity, adapter, song))
                .create()
                .show();
    }

    /**
     * Updates the favorite status of a song.
     *
     * @param song The song to update the favorite status of.
     */
    public static void favorite(final Activity activity, final RecyclerView.Adapter adapter, final Song song) {
        final int songId = song.getId();
        App.getApiClient().favoriteSong(String.valueOf(songId), new FavoriteSongCallback() {
            @Override
            public void onSuccess() {
                if (App.getRadioViewModel().getCurrentSong().getId() == songId) {
                    App.getRadioViewModel().setIsFavorited(true);
                }

                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        song.setFavorite(true);
                        adapter.notifyDataSetChanged();

                        // Broadcast event
                        final Intent favIntent = new Intent(UserFragment.FAVORITE_EVENT);
                        activity.sendBroadcast(favIntent);
                    });
                }
            }

            @Override
            public void onFailure(final String message) {
                if (activity != null) {
                    activity.runOnUiThread(() -> Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_SHORT).show());
                }
            }
        });
    }


    /**
     * Updates the favorite status of a song.
     *
     * @param song The song to update the favorite status of.
     */
    public static void unfavorite(final Activity activity, final RecyclerView.Adapter adapter, final Song song) {
        final int songId = song.getId();
        App.getApiClient().unfavoriteSong(String.valueOf(songId), new FavoriteSongCallback() {
            @Override
            public void onSuccess() {
                if (App.getRadioViewModel().getCurrentSong().getId() == songId) {
                    App.getRadioViewModel().setIsFavorited(false);
                }

                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        song.setFavorite(false);
                        adapter.notifyDataSetChanged();

                        // Broadcast event
                        final Intent favIntent = new Intent(UserFragment.FAVORITE_EVENT);
                        activity.sendBroadcast(favIntent);

                        // Undo action
                        final View coordinatorLayout = activity.findViewById(R.id.coordinator_layout);
                        if (coordinatorLayout != null) {
                            final Snackbar undoBar = Snackbar.make(coordinatorLayout,
                                    String.format(activity.getString(R.string.unfavorited), song.getTitle()),
                                    Snackbar.LENGTH_LONG);
                            undoBar.setAction(R.string.action_undo, (v) -> favorite(activity, adapter, song));
                            undoBar.show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(final String message) {
                if (activity != null) {
                    activity.runOnUiThread(() -> Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_SHORT).show());
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
        final int requests = App.getUserViewModel().getUser().getRequestsRemaining();
        if (requests <= 0) {
            Toast.makeText(activity.getApplicationContext(), R.string.no_requests_left, Toast.LENGTH_SHORT).show();
            return;
        }

        App.getApiClient().requestSong(String.valueOf(song.getId()), new RequestSongCallback() {
            @Override
            public void onSuccess() {
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();

                        // Broadcast event
                        final Intent reqEvent = new Intent(UserFragment.REQUEST_EVENT);
                        activity.sendBroadcast(reqEvent);

                        // TODO: this doesn't update the user card because it's not a VM field
                        final int remainingReqs = requests - 1;
                        App.getUserViewModel().getUser().setRequestsRemaining(remainingReqs);

                        Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.requested_song, song.getTitle()), Toast.LENGTH_LONG).show();
                    });
                }
            }

            @Override
            public void onFailure(final String message) {
                if (activity != null) {
                    activity.runOnUiThread(() -> Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    public static void copyToClipboard(final Context context, final Song song) {
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
