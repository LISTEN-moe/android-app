package me.echeung.moemoekyun.util.ext

import java.util.Locale

fun Long.formatDuration(): String {
    val minutes = this / 60
    val seconds = this % 60
    return if (minutes < 60) {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    } else {
        val hours = minutes / 60
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes % 60, seconds)
    }
}
