package me.echeung.moemoekyun.service

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.activity.MainActivity
import me.echeung.moemoekyun.util.system.notificationManager

object EventNotification {

    private const val NOTIFICATION_ID = 2

    const val NOTIFICATION_CHANNEL_NAME = "Events"
    const val NOTIFICATION_CHANNEL_ID = "events"

    fun notify(eventName: String) {
        val context = App.context

        val action = Intent(context, MainActivity::class.java)
        action.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        val clickIntent = PendingIntent.getActivity(context, 0, action, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_icon)
                .setContentIntent(clickIntent)
                .setContentTitle(context.getString(R.string.event_start_title))
                .setContentText(eventName)

        val notification = builder.build()

        context.notificationManager.notify(NOTIFICATION_ID, notification)
    }

}
