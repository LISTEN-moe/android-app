package me.echeung.moemoekyun.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import me.echeung.moemoekyun.util.ext.formatDuration

private const val PROGRESS_BAR_ALPHA = 0.85f
private const val TRACK_ALPHA_FRACTION = 0.25f

/**
 * Returns a [0, 1] progress value that ticks in real-time based on wall-clock elapsed time.
 * Returns 0 if duration or start time is unknown.
 */
@Composable
fun rememberSongProgress(startTimeEpochMs: Long?, durationSeconds: Long): Float {
    var progress by remember(startTimeEpochMs, durationSeconds) {
        mutableFloatStateOf(
            if (startTimeEpochMs != null && durationSeconds > 0L) {
                computeProgress(startTimeEpochMs, durationSeconds)
            } else {
                0f
            },
        )
    }

    LaunchedEffect(startTimeEpochMs, durationSeconds) {
        if (startTimeEpochMs == null || durationSeconds <= 0L) {
            progress = 0f
            return@LaunchedEffect
        }
        while (progress < 1f) {
            progress = computeProgress(startTimeEpochMs, durationSeconds)
            delay(500L)
        }
    }

    return progress
}

private fun computeProgress(startTimeEpochMs: Long, durationSeconds: Long): Float {
    val elapsed = (System.currentTimeMillis() - startTimeEpochMs) / 1000f
    // If elapsed exceeds duration the start time is stale (belongs to the previous song).
    // Return 0 so the bar resets and waits for the next SSE event.
    if (elapsed > durationSeconds) return 0f
    return (elapsed / durationSeconds).coerceAtLeast(0f)
}

/** Thin full-width bar for the top edge of the collapsed player strip. No corner radius. */
@Composable
fun CollapsedSongProgressBar(progress: Float, modifier: Modifier = Modifier) {
    SongProgressBar(
        progress = progress,
        trackHeight = 2.dp,
        cornerRadius = 0.dp,
        modifier = modifier.fillMaxWidth(),
    )
}

/** Thicker rounded bar with elapsed / total time labels for the expanded player. */
@Composable
fun ExpandedSongProgressBar(progress: Float, durationSeconds: Long, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SongProgressBar(
            progress = progress,
            trackHeight = 6.dp,
            cornerRadius = 3.dp,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val elapsed = if (durationSeconds > 0L) (progress * durationSeconds).toLong().formatDuration() else ""
            val total = if (durationSeconds > 0L) durationSeconds.formatDuration() else ""
            Text(
                text = elapsed,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = total,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun SongProgressBar(progress: Float, trackHeight: Dp, cornerRadius: Dp, modifier: Modifier = Modifier) {
    val accentColor = LocalAlbumArtAccentColor.current
    val primary = MaterialTheme.colorScheme.primary
    val fillColor = remember(accentColor, primary) {
        (accentColor ?: primary).copy(alpha = PROGRESS_BAR_ALPHA)
    }
    val trackColor = remember(fillColor) {
        fillColor.copy(alpha = fillColor.alpha * TRACK_ALPHA_FRACTION)
    }

    Canvas(modifier = modifier.height(trackHeight)) {
        val radius = CornerRadius(cornerRadius.toPx())

        drawRoundRect(color = trackColor, cornerRadius = radius)

        val fillWidth = size.width * progress.coerceIn(0f, 1f)
        if (fillWidth > 0f) {
            drawRoundRect(
                color = fillColor,
                size = Size(fillWidth, size.height),
                cornerRadius = radius,
            )
        }
    }
}
