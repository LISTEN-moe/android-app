package me.echeung.moemoekyun.util.ext

import kotlin.time.Duration.Companion.seconds

fun Long.formatDuration(): String = seconds.toComponents { hours, minutes, seconds, _ ->
    buildString {
        if (hours > 0) append("$hours:")
        append(if (hours > 0) minutes.toString().padStart(2, '0') else minutes)
        append(":${seconds.toString().padStart(2, '0')}")
    }
}
