package me.echeung.moemoekyun.util.ext

import android.app.Activity
import android.app.AlarmManager
import android.app.UiModeManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes

/**
 * Starts an activity.
 *
 * @param context Package context.
 */
inline fun <reified T : Activity> Activity.startActivity() {
    startActivity(Intent(this, T::class.java))
}

/**
 * Finish an activity with a result code.
 *
 * @param resultCode Result code passed back to the activity that started this activity.
 */
fun Activity.finish(resultCode: Int) {
    val returnIntent = Intent()
    setResult(resultCode, returnIntent)
    finish()
}

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
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

/**
 * Checks if the device is currently running in Android Auto mode.
 */
fun Context.isAndroidAuto(): Boolean {
    val uiModeManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
    return uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_CAR
}

val Context.connectivityManager: ConnectivityManager
    get() = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

val Context.alarmManager: AlarmManager
    get() = getSystemService(Context.ALARM_SERVICE) as AlarmManager

val Context.clipboardManager: ClipboardManager
    get() = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

val Context.audioManager: AudioManager
    get() = getSystemService(Context.AUDIO_SERVICE) as AudioManager
