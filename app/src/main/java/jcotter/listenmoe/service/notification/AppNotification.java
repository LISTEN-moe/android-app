package jcotter.listenmoe.service.notification;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import jcotter.listenmoe.R;
import jcotter.listenmoe.model.Song;
import jcotter.listenmoe.service.StreamService;
import jcotter.listenmoe.ui.MenuActivity;
import jcotter.listenmoe.ui.RadioActivity;
import jcotter.listenmoe.util.AuthUtil;

public class AppNotification {
    private int NOTIFICATION_ID = 1;
    private StreamService service;

    private int actionRequestCode = 0;

    public void init(StreamService service) {
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

        // Construct content string with song title and anime
        final StringBuilder titleBuilder = new StringBuilder();
        titleBuilder.append(song.getTitle());
        final String currentSongAnime = song.getAnime();
        if (!currentSongAnime.equals("")) {
            titleBuilder.append("\n");
            titleBuilder.append(String.format("[ %s ]", currentSongAnime));
        }
        final String title = titleBuilder.toString();

        // Play/pause action
        final NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
                isPlaying ? R.drawable.icon_pause : R.drawable.icon_play,
                isPlaying ? service.getString(R.string.action_pause) : service.getString(R.string.action_play),
                getPlaybackActionService(StreamService.class, StreamService.PLAY, !isPlaying)
        );

        // Favorite action
        NotificationCompat.Action favoriteAction;
        if (AuthUtil.isAuthenticated(service)) {
            favoriteAction = new NotificationCompat.Action(
                    song.isFavorite() ? R.drawable.favorite_full : R.drawable.favorite_empty,
                    song.isFavorite() ? service.getString(R.string.action_unfavorite) : service.getString(R.string.action_favorite),
                    getPlaybackActionService(StreamService.class, StreamService.FAVORITE, true)
            );
        } else {
            favoriteAction = new NotificationCompat.Action(
                    R.drawable.favorite_empty,
                    service.getString(R.string.action_favorite),
                    getPlaybackActionActivity(MenuActivity.class, MenuActivity.TAB_INDEX, 2)
            );
        }

        // Stop action
        final NotificationCompat.Action stopAction = new NotificationCompat.Action(
                R.drawable.icon_close,
                service.getString(R.string.action_stop),
                getPlaybackActionService(StreamService.class, StreamService.STOP, true)
        );

        // Build the notification
        final Intent action = new Intent(service, RadioActivity.class);
        action.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent clickIntent = PendingIntent.getActivity(service, 0, action, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(service)
                .setSmallIcon(R.drawable.icon_notification)
                .setColor(ContextCompat.getColor(service, R.color.colorAccent))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(title))
                .setContentTitle(song.getArtist())
                .setContentText(title)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(clickIntent)
                .setOngoing(StreamService.isStreamStarted)
                .addAction(playPauseAction)
                .addAction(favoriteAction)
                .addAction(stopAction);

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
