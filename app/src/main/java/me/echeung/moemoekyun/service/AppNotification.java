package me.echeung.moemoekyun.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;

import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.api.v3.model.Song;
import me.echeung.moemoekyun.ui.activities.MainActivity;
import me.echeung.moemoekyun.utils.AuthUtil;
import me.echeung.moemoekyun.viewmodels.AppViewModel;

public class AppNotification {

    private static final String NOTIFICATION_CHANNEL = "notif_channel";
    private static final int NOTIFICATION_ID = 1;

    private RadioService service;
    private NotificationManager notificationManager;

    public AppNotification(RadioService service) {
        this.service = service;
        notificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void update() {
        if (!service.isStreamStarted()) {
            return;
        }

        final Song song = AppViewModel.getInstance().getCurrentSong();
        if (song == null) {
            return;
        }

        final boolean isPlaying = service.isPlaying();
        final String text = song.getArtistAndAnime();

        // Play/pause action
        final NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
                isPlaying ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp,
                isPlaying ? service.getString(R.string.action_pause) : service.getString(R.string.action_play),
                getPlaybackActionService(RadioService.PLAY_PAUSE)
        );

        // Favorite action
        NotificationCompat.Action favoriteAction;
        if (AuthUtil.isAuthenticated(service)) {
            favoriteAction = new NotificationCompat.Action(
                    song.isFavorite() ? R.drawable.ic_star_white_24dp : R.drawable.ic_star_border_white_24dp,
                    song.isFavorite() ? service.getString(R.string.action_unfavorite) : service.getString(R.string.action_favorite),
                    getPlaybackActionService(RadioService.TOGGLE_FAVORITE)
            );
        } else {
            favoriteAction = new NotificationCompat.Action(
                    R.drawable.ic_star_border_white_24dp,
                    service.getString(R.string.action_favorite),
                    getPlaybackActionActivity(MainActivity.TRIGGER_LOGIN_AND_FAVORITE)
            );
        }

        // Build the notification
        final Intent action = new Intent(service, MainActivity.class);
        action.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent clickIntent = PendingIntent.getActivity(service, 0, action, PendingIntent.FLAG_UPDATE_CURRENT);
        final PendingIntent deleteIntent = getPlaybackActionService(RadioService.STOP);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(service, NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.icon_notification)
                .setContentTitle(song.getTitle())
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(clickIntent)
                .setDeleteIntent(deleteIntent)
                .setOngoing(isPlaying)
                .setShowWhen(false)
                .addAction(playPauseAction)
                .addAction(favoriteAction);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            builder.setStyle(new MediaStyle().setShowActionsInCompactView(0, 1));
            builder.setColor(ContextCompat.getColor(service, R.color.colorAccent));
        }

        final Notification notification = builder.build();

        if (isPlaying) {
            service.startForeground(NOTIFICATION_ID, notification);
        } else {
            service.stopForeground(false);
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private PendingIntent getPlaybackActionService(final String action) {
        final Intent intent = new Intent(service, RadioService.class);
        intent.setAction(action);

        return PendingIntent.getService(service, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getPlaybackActionActivity(final String action) {
        final Intent intent = new Intent(service, MainActivity.class);
        intent.setAction(action);

        return PendingIntent.getActivity(service, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
