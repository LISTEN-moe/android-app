package me.echeung.moemoekyun.util.ext

import android.Manifest
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat

fun NotificationManagerCompat.notifyIfPermitted(context: Context, id: Int, notification: Notification) {
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        notify(id, notification)
    }
}
