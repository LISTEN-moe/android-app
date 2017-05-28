package jcotter.listenmoe.service.notification;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;

import jcotter.listenmoe.R;
import jcotter.listenmoe.model.Song;
import jcotter.listenmoe.service.StreamService;
import jcotter.listenmoe.ui.activities.MainActivity;
import jcotter.listenmoe.ui.activities.MenuActivity;
import jcotter.listenmoe.util.AuthUtil;

public class AppNotification {
    private int NOTIFICATION_ID = 1;
    private StreamService service;

    private int actionRequestCode;

    public AppNotification(StreamService service) {
        this.service = service;
    }

    public void update() {
        if (!StreamService.isStreamStarted) {
            return;
        }

        // Get current song info
        final Song song = service.getCurrentSong();
        if (song == null) {
            return;
        }

        final boolean isPlaying = service.isPlaying();

        // Construct content string with song artist and anime
        final StringBuilder textBuilder = new StringBuilder();
        textBuilder.append(song.getArtist());
        final String currentSongAnime = song.getAnime();
        if (!currentSongAnime.equals("")) {
            textBuilder.append(String.format(" [ %s ]", currentSongAnime));
        }
        final String text = textBuilder.toString();

        actionRequestCode = 0;

        // Play/pause action
        final NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
                isPlaying ? R.drawable.ic_pause_black_24dp : R.drawable.ic_play_arrow_black_24dp,
                isPlaying ? service.getString(R.string.action_pause) : service.getString(R.string.action_play),
                getPlaybackActionService(StreamService.class, StreamService.PLAY, !isPlaying)
        );

        // Favorite action
        NotificationCompat.Action favoriteAction;
        if (AuthUtil.isAuthenticated(service)) {
            favoriteAction = new NotificationCompat.Action(
                    song.isFavorite() ? R.drawable.ic_star_black_24dp : R.drawable.ic_star_border_black_24dp,
                    song.isFavorite() ? service.getString(R.string.action_unfavorite) : service.getString(R.string.action_favorite),
                    getPlaybackActionService(StreamService.class, StreamService.FAVORITE, true)
            );
        } else {
            // TODO: trigger MainActivity login dialog
            favoriteAction = new NotificationCompat.Action(
                    R.drawable.ic_star_border_black_24dp,
                    service.getString(R.string.action_favorite),
                    getPlaybackActionActivity(MenuActivity.class, MenuActivity.TAB_INDEX, 2)
            );
        }

        // Stop action
        final NotificationCompat.Action stopAction = new NotificationCompat.Action(
                R.drawable.ic_stop_black_24dp,
                service.getString(R.string.action_stop),
                getPlaybackActionService(StreamService.class, StreamService.STOP, true)
        );

        // Build the notification
        final Intent action = new Intent(service, MainActivity.class);
        action.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent clickIntent = PendingIntent.getActivity(service, 0, action, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(service)
                .setSmallIcon(R.drawable.icon_notification)
                .setContentTitle(song.getTitle())
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(clickIntent)
                .setOngoing(StreamService.isStreamStarted)
                .setShowWhen(false)
                .addAction(playPauseAction)
                .addAction(favoriteAction)
                .addAction(stopAction);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Visibility
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            // Special media style
            builder.setStyle(new NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2));

            // Text/icon color
            builder.setColor(ContextCompat.getColor(service, R.color.colorAccent));
        }

        service.startForeground(NOTIFICATION_ID, builder.build());
    }

    private PendingIntent getPlaybackActionService(final Class target, final String action, final boolean value) {
        final Intent intent = new Intent(service, target);
        intent.putExtra(action, value);

        return PendingIntent.getService(service, ++actionRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // TODO: possibly move this since it's related to the activity instead of the service
    private PendingIntent getPlaybackActionActivity(final Class target, final String action, final int value) {
        final Intent intent = new Intent(service, target);
        intent.putExtra(action, value);

        return PendingIntent.getActivity(service, ++actionRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
