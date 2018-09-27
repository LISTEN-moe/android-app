package me.echeung.moemoekyun.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.ui.activity.MainActivity
import me.echeung.moemoekyun.util.AlbumArtUtil

class AppNotification internal constructor(private val service: RadioService) {

    private val notificationManager: NotificationManager =
            service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val currentSong: Song?
        get() = App.radioViewModel!!.currentSong

    fun update() {
        if (!service.isStreamStarted) {
            return
        }

        val song = currentSong
        val albumArt = AlbumArtUtil.getCurrentAlbumArt(250)

        val isPlaying = service.isPlaying

        // Play/pause action
        val playPauseAction = NotificationCompat.Action(
                if (isPlaying) R.drawable.ic_pause_white_24dp else R.drawable.ic_play_arrow_white_24dp,
                if (isPlaying) service.getString(R.string.action_pause) else service.getString(R.string.action_play),
                getPlaybackActionService(RadioService.PLAY_PAUSE))

        // Build the notification
        val action = Intent(service, MainActivity::class.java)
        action.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        val clickIntent = PendingIntent.getActivity(service, 0, action, PendingIntent.FLAG_UPDATE_CURRENT)
        val deleteIntent = getPlaybackActionService(RadioService.STOP)

        val style = MediaStyle().setMediaSession(service.mediaSession.sessionToken)

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

        // For pre-Oreo colored notifications
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && !AlbumArtUtil.isDefaultAlbumArt()) {
            builder.color = AlbumArtUtil.getCurrentAccentColor()
        }

        // Needs to be set after setting the color
        builder.setStyle(style.setShowActionsInCompactView(0))

        if (song != null) {
            builder.setContentTitle(song.titleString)
            builder.setContentText(song.artistsString)
            builder.setSubText(song.albumsString)

            // Add favorite action if logged in
            if (App.authUtil.isAuthenticated) {
                builder.addAction(NotificationCompat.Action(
                        if (song.isFavorite) R.drawable.ic_star_white_24dp else R.drawable.ic_star_border_white_24dp,
                        if (song.isFavorite) service.getString(R.string.action_unfavorite) else service.getString(R.string.action_favorite),
                        getPlaybackActionService(RadioService.TOGGLE_FAVORITE)))

                builder.setStyle(style.setShowActionsInCompactView(0, 1))
            }
        }

        val notification = builder.build()

        if (isPlaying) {
            service.startForeground(NOTIFICATION_ID, notification)
        } else {
            service.stopForeground(false)
        }

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun getPlaybackActionService(action: String): PendingIntent {
        val intent = Intent(service, RadioService::class.java)
        intent.action = action

        return PendingIntent.getService(service, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    companion object {
        const val NOTIFICATION_CHANNEL_NAME = "Default"
        const val NOTIFICATION_CHANNEL_ID = "default"

        private const val NOTIFICATION_ID = 1
    }

}
