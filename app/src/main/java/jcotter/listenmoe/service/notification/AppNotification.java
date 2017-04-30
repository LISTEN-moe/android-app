package jcotter.listenmoe.service.notification;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
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

    public void init(StreamService service) {
        this.service = service;
    }

    public void update() {
        if (!StreamService.isStreamStarted) {
            return;
        }

        final Song song = service.getCurrentSong();

        if (song == null) {
            return;
        }

        final boolean isPlaying = service.isPlaying();

        final Intent intent = new Intent(service, RadioActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(service, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(service)
                .setContentTitle(song.getArtist())
                .setSmallIcon(R.drawable.icon_notification)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(service, R.color.colorAccent));

        // Construct string with song title and anime
        final String currentSongAnime = song.getAnime();
        String title = song.getTitle();
        if (!currentSongAnime.equals("")) {
            title += "\n" + "[" + currentSongAnime + "]";
        }
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(title));
        builder.setContentText(title);

        // Play/pause button
        final Intent playPauseIntent = new Intent(service, service.getClass());
        PendingIntent playPausePending;
        if (isPlaying) {
            playPauseIntent.putExtra(StreamService.PLAY, false);
            playPausePending = PendingIntent.getService(service, 1, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
                builder.addAction(new NotificationCompat.Action.Builder(R.drawable.icon_pause, "", playPausePending).build());
            else
                builder.addAction(new NotificationCompat.Action.Builder(R.drawable.icon_pause, service.getString(R.string.action_pause), playPausePending).build());
        } else {
            playPauseIntent.putExtra(StreamService.PLAY, true);
            playPausePending = PendingIntent.getService(service, 1, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
                builder.addAction(new NotificationCompat.Action.Builder(R.drawable.icon_play, "", playPausePending).build());
            else
                builder.addAction(new NotificationCompat.Action.Builder(R.drawable.icon_play, service.getString(R.string.action_play), playPausePending).build());
        }

        // Favorite button
        final Intent favoriteIntent = new Intent(service, service.getClass());
        favoriteIntent.putExtra(StreamService.FAVORITE, true);
        PendingIntent favoritePending = PendingIntent.getService(service, 2, favoriteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (!AuthUtil.isAuthenticated(service)) {
            final Intent authIntent = new Intent(service, MenuActivity.class);
            authIntent.putExtra("index", 2);
            PendingIntent authPending = PendingIntent.getActivity(service, 3, authIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
                builder.addAction(new NotificationCompat.Action.Builder(R.drawable.favorite_empty, "", authPending).build());
            else
                builder.addAction(new NotificationCompat.Action.Builder(R.drawable.favorite_empty, service.getString(R.string.action_favorite), authPending).build());
        } else {
            if (song.isFavorite())
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
                    builder.addAction(new NotificationCompat.Action.Builder(R.drawable.favorite_full, "", favoritePending).build());
                else
                    builder.addAction(new NotificationCompat.Action.Builder(R.drawable.favorite_full, service.getString(R.string.action_unfavorite), favoritePending).build());
            else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
                builder.addAction(new NotificationCompat.Action.Builder(R.drawable.favorite_empty, "", favoritePending).build());
            else
                builder.addAction(new NotificationCompat.Action.Builder(R.drawable.favorite_empty, service.getString(R.string.action_favorite), favoritePending).build());
        }

        // Stop button
        final Intent stopIntent = new Intent(service, service.getClass());
        stopIntent.putExtra(StreamService.STOP, true);
        PendingIntent stopPending = PendingIntent.getService(service, 4, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            builder.addAction(new NotificationCompat.Action.Builder(R.drawable.icon_close, "", stopPending).build());
        else
            builder.addAction(new NotificationCompat.Action.Builder(R.drawable.icon_close, service.getString(R.string.action_stop), stopPending).build());

        service.startForeground(NOTIFICATION_ID, builder.build());
    }

    public void stop() {
        service.stop();
    }
}
