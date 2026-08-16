package me.echeung.moemoekyun.util.ext

import kotlin.time.Duration.Companion.seconds

/**
 * Elapsed play time in ms for a song, clamped to [0, duration].
 *
 * The start time comes from the SSE metadata stream (the song's true start), so a non-null value
 * always belongs to the current song. This only clamps the value into range so callers extrapolating
 * from it stay stable at the song's start and end.
 */
fun songElapsedMs(startTimeEpochMs: Long, durationSeconds: Long, nowMs: Long = System.currentTimeMillis()): Long {
    val elapsedMs = nowMs - startTimeEpochMs
    val durationMs = durationSeconds.seconds.inWholeMilliseconds
    return when {
        elapsedMs < 0 -> 0
        durationMs > 0 -> elapsedMs.coerceAtMost(durationMs)
        else -> elapsedMs
    }
}

fun Long.formatDuration(): String = seconds.toComponents { hours, minutes, seconds, _ ->
    buildString {
        if (hours > 0) append("$hours:")
        append(if (hours > 0) minutes.toString().padStart(2, '0') else minutes)
        append(":${seconds.toString().padStart(2, '0')}")
    }
}
