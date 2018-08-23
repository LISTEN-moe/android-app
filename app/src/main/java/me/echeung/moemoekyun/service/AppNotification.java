package me.echeung.moemoekyun.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.client.model.Song;
import me.echeung.moemoekyun.ui.activity.MainActivity;
import me.echeung.moemoekyun.util.AlbumArtUtil;

public class AppNotification {

    public static final String NOTIFICATION_CHANNEL_NAME = "Default";
    public static final String NOTIFICATION_CHANNEL_ID = "default";

    private static final int NOTIFICATION_ID = 1;

    private RadioService service;
    private NotificationManager notificationManager;

    AppNotification(RadioService service) {
        this.service = service;
        notificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    void update() {
        if (!service.isStreamStarted()) {
            return;
        }

        Song song = getCurrentSong();
        Bitmap albumArt = AlbumArtUtil.getCurrentAlbumArt(250);

        boolean isPlaying = service.isPlaying();

        // Play/pause action
        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
                isPlaying ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp,
                isPlaying ? service.getString(R.string.action_pause) : service.getString(R.string.action_play),
                getPlaybackActionService(RadioService.PLAY_PAUSE));

        // Build the notification
        Intent action = new Intent(service, MainActivity.class);
        action.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent clickIntent = PendingIntent.getActivity(service, 0, action, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent deleteIntent = getPlaybackActionService(RadioService.STOP);

        androidx.media.app.NotificationCompat.MediaStyle style = new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(service.getMediaSession().getSessionToken());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(service, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_icon)
                .setLargeIcon(albumArt)
                .setContentIntent(clickIntent)
                .setDeleteIntent(deleteIntent)
                .addAction(playPauseAction)
                .setContentTitle(service.getString(R.string.app_name))
                .setOngoing(isPlaying)
                .setShowWhen(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true);

        // For pre-Oreo colored notifications
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && !AlbumArtUtil.isDefaultAlbumArt()) {
            builder.setColor(AlbumArtUtil.getCurrentAccentColor());
        }

        // Needs to be set after setting the color
        builder.setStyle(style.setShowActionsInCompactView(0));

        if (song != null) {
            builder.setContentTitle(song.getTitleString());
            builder.setContentText(song.getArtistsString());
            builder.setSubText(song.getAlbumsString());

            // Add favorite action if logged in
            if (App.getAuthUtil().isAuthenticated()) {
                builder.addAction(new NotificationCompat.Action(
                        song.isFavorite() ? R.drawable.ic_star_white_24dp : R.drawable.ic_star_border_white_24dp,
                        song.isFavorite() ? service.getString(R.string.action_unfavorite) : service.getString(R.string.action_favorite),
                        getPlaybackActionService(RadioService.TOGGLE_FAVORITE)));

                builder.setStyle(style.setShowActionsInCompactView(0, 1));
            }
        }

        Notification notification = builder.build();

        if (isPlaying) {
            service.startForeground(NOTIFICATION_ID, notification);
        } else {
            service.stopForeground(false);
        }

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private Song getCurrentSong() {
        return App.getRadioViewModel().getCurrentSong();
    }

    private PendingIntent getPlaybackActionService(String action) {
        Intent intent = new Intent(service, RadioService.class);
        intent.setAction(action);

        return PendingIntent.getService(service, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
