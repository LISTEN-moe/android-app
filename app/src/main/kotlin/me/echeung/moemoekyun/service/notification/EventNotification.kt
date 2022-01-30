package me.echeung.moemoekyun.service.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.activity.MainActivity

object EventNotification {

    private const val NOTIFICATION_ID = 2

    const val NOTIFICATION_CHANNEL_NAME = "Events"
    const val NOTIFICATION_CHANNEL_ID = "events"

    fun notify(context: Context, eventName: String) {
        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_icon)
            .setContentIntent(getOpenAppIntent(context))
            .setContentTitle(context.getString(R.string.event_start_title))
            .setContentText(eventName)

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
    }

    private fun getOpenAppIntent(context: Context): PendingIntent {
        val action = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        return PendingIntent.getActivity(context, 0, action, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
