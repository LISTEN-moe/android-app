package me.echeung.moemoekyun.service.notification

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.app.NotificationCompat.MediaStyle
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.api.model.Song
import me.echeung.moemoekyun.service.RadioService
import me.echeung.moemoekyun.ui.activity.MainActivity
import me.echeung.moemoekyun.util.AlbumArtUtil

class MusicNotifier internal constructor(
    private val service: RadioService,
    private val albumArtUtil: AlbumArtUtil
) {

    fun update(song: Song?, isAuthenticated: Boolean) {
        if (!service.isStreamStarted) {
            return
        }

        val albumArt = albumArtUtil.getCurrentAlbumArt(250)
        val isPlaying = service.isPlaying

        // Play/pause action
        val playPauseAction = NotificationCompat.Action(
            if (isPlaying) R.drawable.ic_pause_24dp else R.drawable.ic_play_arrow_24dp,
            if (isPlaying) service.getString(R.string.action_pause) else service.getString(R.string.action_play),
            getPlaybackActionService(RadioService.PLAY_PAUSE)
        )

        // Build the notification
        val action = Intent(service, MainActivity::class.java)
        action.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        val clickIntent = PendingIntent.getActivity(service, 0, action, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val deleteIntent = getPlaybackActionService(RadioService.STOP)

        val style = MediaStyle().setMediaSession(service.mediaSession!!.sessionToken)

        val builder = NotificationCompat.Builder(service, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_icon)
            .setLargeIcon(albumArt)
            .setContentIntent(clickIntent)
            .setDeleteIntent(deleteIntent)
            .addAction(playPauseAction)
            .setContentTitle(service.getString(R.string.app_name))
            .setOngoing(isPlaying)
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)

        // Pre-Oreo (Android 8.x) colored notifications
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && !albumArtUtil.isDefaultAlbumArt) {
            builder.color = albumArtUtil.currentAccentColor
        }

        // Needs to be set after setting the color
        builder.setStyle(style.setShowActionsInCompactView(0))

        if (song != null) {
            builder.setContentTitle(song.getTitleString())
            builder.setContentText(song.getArtistsString())
            builder.setSubText(song.getAlbumsString())

            // Add favorite action if logged in
            if (isAuthenticated) {
                builder.addAction(
                    NotificationCompat.Action(
                        if (song.favorite) R.drawable.ic_star_24dp else R.drawable.ic_star_border_24dp,
                        if (song.favorite) service.getString(R.string.action_unfavorite) else service.getString(R.string.action_favorite),
                        getPlaybackActionService(RadioService.TOGGLE_FAVORITE)
                    )
                )

                builder.setStyle(style.setShowActionsInCompactView(0, 1))
            }
        }

        val notification = builder.build()

        if (isPlaying) {
            service.startForeground(NOTIFICATION_ID, notification)
        } else {
            service.stopForeground(false)
        }

        NotificationManagerCompat.from(service).notify(NOTIFICATION_ID, notification)
    }

    private fun getPlaybackActionService(intentAction: String): PendingIntent {
        val intent = Intent(service, RadioService::class.java).apply {
            action = intentAction
        }

        return PendingIntent.getService(service, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {
        const val NOTIFICATION_CHANNEL_NAME = "Default"
        const val NOTIFICATION_CHANNEL_ID = "default"
    }
}

private const val NOTIFICATION_ID = 1
