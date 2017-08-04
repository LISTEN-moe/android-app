package me.echeung.moemoekyun.service.notification;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;

import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.model.Song;
import me.echeung.moemoekyun.service.StreamService;
import me.echeung.moemoekyun.ui.App;
import me.echeung.moemoekyun.ui.activities.MainActivity;
import me.echeung.moemoekyun.util.AuthUtil;

public class AppNotification {

    private static final String NOTIFICATION_CHANNEL = "notif_channel";
    private static final int NOTIFICATION_ID = 1;
    private StreamService service;

    private int actionRequestCode;

    public AppNotification(StreamService service) {
        this.service = service;
    }

    public void update() {
        if (!service.isStreamStarted()) {
            return;
        }

        // Get current song info
        final Song song = App.STATE.currentSong.get();
        if (song == null) {
            return;
        }

        final boolean isPlaying = service.isPlaying();
        final String text = song.getArtistAndAnime();

        actionRequestCode = 0;

        // Play/pause action
        final NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
                isPlaying ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp,
                isPlaying ? service.getString(R.string.action_pause) : service.getString(R.string.action_play),
                getPlaybackActionService(StreamService.class, StreamService.PLAY, !isPlaying)
        );

        // Favorite action
        NotificationCompat.Action favoriteAction;
        if (AuthUtil.isAuthenticated(service)) {
            favoriteAction = new NotificationCompat.Action(
                    song.isFavorite() ? R.drawable.ic_star_white_24dp : R.drawable.ic_star_border_white_24dp,
                    song.isFavorite() ? service.getString(R.string.action_unfavorite) : service.getString(R.string.action_favorite),
                    getPlaybackActionService(StreamService.class, StreamService.FAVORITE, true)
            );
        } else {
            favoriteAction = new NotificationCompat.Action(
                    R.drawable.ic_star_border_white_24dp,
                    service.getString(R.string.action_favorite),
                    getPlaybackActionActivity(MainActivity.class, MainActivity.TRIGGER_LOGIN)
            );
        }

        // Stop action
        final NotificationCompat.Action stopAction = new NotificationCompat.Action(
                R.drawable.ic_stop_white_24dp,
                service.getString(R.string.action_stop),
                getPlaybackActionService(StreamService.class, StreamService.STOP, true)
        );

        // Build the notification
        final Intent action = new Intent(service, MainActivity.class);
        action.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent clickIntent = PendingIntent.getActivity(service, 0, action, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(service, NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.icon_notification)
                .setContentTitle(song.getTitle())
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(clickIntent)
                .setOngoing(service.isStreamStarted())
                .setShowWhen(false)
                .addAction(playPauseAction)
                .addAction(favoriteAction)
                .addAction(stopAction);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            builder.setStyle(new MediaStyle().setShowActionsInCompactView(0, 1, 2));
            builder.setColor(ContextCompat.getColor(service, R.color.colorAccent));
        }

        service.startForeground(NOTIFICATION_ID, builder.build());
    }

    private PendingIntent getPlaybackActionService(final Class target, final String action, final boolean value) {
        final Intent intent = new Intent(service, target);
        intent.putExtra(action, value);

        return PendingIntent.getService(service, ++actionRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getPlaybackActionActivity(final Class target, final String action) {
        final Intent intent = new Intent(service, target);
        intent.setAction(action);

        return PendingIntent.getActivity(service, ++actionRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
