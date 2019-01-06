package me.echeung.moemoekyun.util.system

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.UiModeManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.widget.Toast
import androidx.annotation.StringRes

/**
 * Gets a plural string resource using the given quantity value.
 *
 * @param resource the text resource.
 * @param value the quantity used to choose the proper plural format.
 */
fun Context.getPluralString(resource: Int, value: Int): String {
    val text = resources.getQuantityString(resource, value)
    return String.format(text, value)
}

/**
 * Display a toast in this context.
 *
 * @param resource the text resource.
 * @param duration the duration of the toast. Defaults to short.
 */
fun Context.toast(@StringRes resource: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, resource, duration).show()
}

/**
 * Display a toast in this context.
 *
 * @param text the text to display.
 * @param duration the duration of the toast. Defaults to short.
 */
fun Context.toast(text: String?, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text.orEmpty(), duration).show()
}

/**
 * Opens a URL in a browser.
 *
 * @param url Page URL to open.
 */
fun Context.openUrl(url: String) {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(browserIntent)
}

/**
 * Checks if the device is currently running in Android Auto mode.
 */
fun Context.isCarUiMode(): Boolean {
    val uiModeManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
    return uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_CAR
}

/**
 * Property to get the notification manager from the context.
 */
val Context.notificationManager: NotificationManager
    get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

/**
 * Property to get the connectivity manager from the context.
 */
val Context.connectivityManager: ConnectivityManager
    get() = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

/**
 * Property to get the wifi manager from the context.
 */
val Context.wifiManager: WifiManager
    get() = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

/**
 * Property to get the alarm manager from the context.
 */
val Context.alarmManager: AlarmManager
    get() = getSystemService(Context.ALARM_SERVICE) as AlarmManager

/**
 * Property to get the clipboard manager from the context.
 */
val Context.clipboardManager: ClipboardManager
    get() = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
