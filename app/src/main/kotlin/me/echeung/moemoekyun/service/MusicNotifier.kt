package me.echeung.moemoekyun.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.stream.Stream
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.ui.MainActivity
import me.echeung.moemoekyun.util.AlbumArtUtil
import javax.inject.Inject

class MusicNotifier @Inject constructor(
    private val albumArtUtil: AlbumArtUtil,
) {

    @SuppressLint("MissingPermission")
    fun update(service: AppService, currentSong: DomainSong?, streamState: Stream.State, isAuthenticated: Boolean) {
        if (currentSong == null || service.mediaSession == null || streamState == Stream.State.STOP) {
            return
        }

        // Play/pause action
        val isPlaying = streamState == Stream.State.PLAY
        val playPauseAction = NotificationCompat.Action(
            if (isPlaying) R.drawable.ic_pause_24dp else R.drawable.ic_play_arrow_24dp,
            if (isPlaying) service.getString(R.string.action_pause) else service.getString(R.string.action_play),
            getPlaybackActionService(service, AppService.PLAY_PAUSE),
        )

        // Build the notification
        val action = Intent(service, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val clickIntent = PendingIntent.getActivity(service, 0, action, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val deleteIntent = getPlaybackActionService(service, AppService.STOP)

        val builder = NotificationCompat.Builder(service, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_icon)
            .setLargeIcon(albumArtUtil.getCurrentAlbumArt(500))
            .setContentIntent(clickIntent)
            .setDeleteIntent(deleteIntent)
            .addAction(playPauseAction)
            .setContentTitle(service.getString(R.string.app_name))
            .setOngoing(isPlaying)
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)

        // Needs to be set after setting the color
        val style = androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(service.mediaSession!!.sessionToken)
        builder.setStyle(style.setShowActionsInCompactView(0))

        builder.setContentTitle(currentSong.title)
        builder.setContentText(currentSong.artists)
        builder.setSubText(currentSong.albums)

        // Add favorite action if logged in
        if (isAuthenticated) {
            builder.addAction(
                NotificationCompat.Action(
                    if (currentSong.favorited) R.drawable.ic_star_24dp else R.drawable.ic_star_border_24dp,
                    if (currentSong.favorited) service.getString(R.string.action_unfavorite) else service.getString(
                        R.string.action_favorite,
                    ),
                    getPlaybackActionService(service, AppService.TOGGLE_FAVORITE),
                ),
            )

            builder.setStyle(style.setShowActionsInCompactView(0, 1))
        }

        val notification = builder.build()

        if (isPlaying) {
            service.startForeground(NOTIFICATION_ID, notification)
        } else {
            service.stopForeground(false)
        }

        NotificationManagerCompat.from(service).notify(NOTIFICATION_ID, notification)
    }

    private fun getPlaybackActionService(service: AppService, intentAction: String): PendingIntent {
        val intent = Intent(service, AppService::class.java).apply {
            action = intentAction
        }

        return PendingIntent.getService(service, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {
        const val NOTIFICATION_CHANNEL_NAME = "Default"
        const val NOTIFICATION_CHANNEL_ID = "default"

        private const val NOTIFICATION_ID = 1
    }
}
