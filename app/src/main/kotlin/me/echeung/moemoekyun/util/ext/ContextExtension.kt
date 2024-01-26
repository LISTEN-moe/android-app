package me.echeung.moemoekyun.util.ext

import android.app.UiModeManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.media.AudioManager
import android.net.ConnectivityManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.getSystemService
import me.echeung.moemoekyun.R

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
 * Copies a string to clipboard
 *
 * @param label Label to show to the user describing the content
 * @param content the actual text to copy to the board
 */
fun Context.copyToClipboard(label: String, content: String) {
    if (content.isBlank()) return

    try {
        val clipboard = getSystemService<ClipboardManager>()!!
        clipboard.setPrimaryClip(ClipData.newPlainText(label, content))

        // Android 13 and higher shows a visual confirmation of copied contents
        // https://developer.android.com/about/versions/13/features/copy-paste
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            toast(getString(R.string.copied_to_clipboard_content, content))
        }
    } catch (_: Throwable) {
    }
}

/**
 * Checks if the device is currently running in Android Auto mode.
 */
fun Context.isCarUiMode(): Boolean {
    val uiModeManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
    return uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_CAR
}

val Context.connectivityManager: ConnectivityManager
    get() = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

val Context.clipboardManager: ClipboardManager
    get() = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

val Context.audioManager: AudioManager
    get() = getSystemService(Context.AUDIO_SERVICE) as AudioManager
