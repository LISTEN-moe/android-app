package me.echeung.moemoekyun.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;

import me.echeung.listenmoeapi.models.Song;
import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.ui.activities.MainActivity;
import me.echeung.moemoekyun.utils.AlbumArtUtil;

public class AppNotification {

    public static final String NOTIFICATION_CHANNEL_NAME = "Default";
    public static final String NOTIFICATION_CHANNEL_ID = "default";

    private static final int NOTIFICATION_ID = 1;

    private static final int NOTIFICATION_ALBUM_ART_MAX_SIZE = 500;

    private RadioService service;
    private NotificationManager notificationManager;

    private int lastSongId;
    private Bitmap lastAlbumArt;

    AppNotification(RadioService service) {
        this.service = service;
        notificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    void update() {
        if (!service.isStreamStarted()) {
            return;
        }

        final Song song = getCurrentSong();

        // Use the already downloaded album art
        Bitmap albumArt = null;
        if (song != null && lastSongId == song.getId() && lastAlbumArt != null) {
            albumArt = lastAlbumArt;
        }

        update(song, albumArt);

        if (song != null && lastSongId != song.getId()) {
            lastSongId = song.getId();

            // Update the notification with the downloaded album art as needed
            AlbumArtUtil.getAlbumArtBitmap(service, song, NOTIFICATION_ALBUM_ART_MAX_SIZE, bitmap -> {
                lastAlbumArt = bitmap;

                update(song, bitmap);
            });
        }
    }

    private void update(final Song song, final Bitmap albumArt) {
        if (!service.isStreamStarted()) {
            return;
        }

        final boolean isPlaying = service.isPlaying();

        // Play/pause action
        final NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
                isPlaying ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp,
                isPlaying ? service.getString(R.string.action_pause) : service.getString(R.string.action_play),
                getPlaybackActionService(RadioService.PLAY_PAUSE)
        );

        // Build the notification
        final Intent action = new Intent(service, MainActivity.class);
        action.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent clickIntent = PendingIntent.getActivity(service, 0, action, PendingIntent.FLAG_UPDATE_CURRENT);
        final PendingIntent deleteIntent = getPlaybackActionService(RadioService.STOP);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(service, NOTIFICATION_CHANNEL_ID)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
                .setColor(ContextCompat.getColor(service, R.color.colorAccent))
                .setContentTitle(service.getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_icon)
                .setLargeIcon(albumArt)
                .setContentIntent(clickIntent)
                .setDeleteIntent(deleteIntent)
                .setOngoing(isPlaying)
                .addAction(playPauseAction)
                .setStyle(new MediaStyle().setShowActionsInCompactView(0));

        if (song != null) {
            builder.setContentTitle(song.getTitle());
            builder.setContentText(song.getArtistString());

            // Add favorite action if logged in
            if (App.getAuthUtil().isAuthenticated()) {
                builder.addAction(new NotificationCompat.Action(
                        song.isFavorite() ? R.drawable.ic_star_white_24dp : R.drawable.ic_star_border_white_24dp,
                        song.isFavorite() ? service.getString(R.string.action_unfavorite) : service.getString(R.string.action_favorite),
                        getPlaybackActionService(RadioService.TOGGLE_FAVORITE)
                ));

                builder.setStyle(new MediaStyle().setShowActionsInCompactView(0, 1));
            } else {
                builder.setStyle(new MediaStyle().setShowActionsInCompactView(0));
            }
        }

        final Notification notification = builder.build();

        if (isPlaying) {
            service.startForeground(NOTIFICATION_ID, notification);
        } else {
            service.stopForeground(false);
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private Song getCurrentSong() {
        return App.getRadioViewModel().getCurrentSong();
    }

    private PendingIntent getPlaybackActionService(final String action) {
        final Intent intent = new Intent(service, RadioService.class);
        intent.setAction(action);

        return PendingIntent.getService(service, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
