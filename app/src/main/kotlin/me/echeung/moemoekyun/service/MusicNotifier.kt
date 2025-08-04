package me.echeung.moemoekyun.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import logcat.LogPriority
import logcat.logcat
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.api.Stream
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.ui.MainActivity
import me.echeung.moemoekyun.util.AlbumArtUtil
import me.echeung.moemoekyun.util.ext.notifyIfPermitted
import javax.inject.Inject

class MusicNotifier @Inject constructor(
    private val albumArtUtil: AlbumArtUtil,
) {

    fun update(service: AppService, currentSong: DomainSong?, streamState: Stream.State) {
        if (currentSong == null || service.mediaSession == null || streamState == Stream.State.STOPPED) {
            return
        }

        // Play/pause action
        val isPlaying = streamState == Stream.State.PLAYING
        val playPauseAction = NotificationCompat.Action(
            if (isPlaying) R.drawable.ic_pause_24dp else R.drawable.ic_play_arrow_24dp,
            if (isPlaying) service.getString(R.string.action_pause) else service.getString(R.string.action_play),
            getPlaybackActionService(service, AppService.PLAY_PAUSE),
        )

        val deleteIntent = getPlaybackActionService(service, AppService.STOP)

        val builder = NotificationCompat.Builder(service, NOTIFICATION_CHANNEL_ID)
            .setLargeIcon(albumArtUtil.getCurrentAlbumArt(500))
            .setDeleteIntent(deleteIntent)
            .addAction(playPauseAction)
            .setContentTitle(service.getString(R.string.app_name))
            .setOngoing(isPlaying)
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)

        // Needs to be set after setting the color
        // TODO: replace with MediaStyleNotificationHelper.MediaStyle
        val style = androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(
            service.mediaSession!!.sessionToken,
        )
        builder.setStyle(style.setShowActionsInCompactView(0))

        builder.setContentTitle(currentSong.title)
        builder.setContentText(currentSong.artists)
        builder.setSubText(currentSong.albums)

        // ForegroundServiceStartNotAllowedException can be thrown in Android 12+
        // if this ends up getting started once the app was already backgrounded.
        try {
            val notification = builder.build()

            if (isPlaying) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    service.startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
                    )
                } else {
                    service.startForeground(NOTIFICATION_ID, notification)
                }
            } else {
                service.stopForeground(Service.STOP_FOREGROUND_DETACH)
            }

            NotificationManagerCompat.from(service).notifyIfPermitted(service, NOTIFICATION_ID, notification)
        } catch (e: IllegalStateException) {
            logcat(LogPriority.ERROR) { "Failed to start foreground service" }
        }
    }

    private fun getPlaybackActionService(service: AppService, intentAction: String): PendingIntent {
        val intent = Intent(service, AppService::class.java).apply {
            action = intentAction
        }

        return PendingIntent.getService(
            service,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val NOTIFICATION_CHANNEL_NAME = "Default"
        const val NOTIFICATION_CHANNEL_ID = "default"

        private const val NOTIFICATION_ID = 1
    }
}
