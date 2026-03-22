package me.echeung.moemoekyun.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import me.echeung.moemoekyun.service.VisualizerState
import kotlin.math.min

private val MaxHeight = 120.dp
private val MinBarWidth = 12.dp
private val BarGap = 2.dp
private val BarCornerRadius = 8.dp
private const val ACCENT_BAR_ALPHA = 0.5f

@Composable
fun AudioVisualizer(state: VisualizerState, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val minBarWidthPx = with(density) { MinBarWidth.toPx() }
    val gapPx = with(density) { BarGap.toPx() }
    val cornerRadiusPx = with(density) { BarCornerRadius.toPx() }

    // Animate each bar independently with spring physics — always BAND_COUNT entries so
    // the number of composable calls is stable across recompositions.
    val animatedMagnitudes = Array(VisualizerState.BAND_COUNT) { i ->
        val animated by animateFloatAsState(
            targetValue = state.magnitudes[i],
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow,
            ),
            label = "bar_$i",
        )
        animated
    }

    val albumArtAccent = LocalAlbumArtAccentColor.current
    val primary = MaterialTheme.colorScheme.primary
    val barColor = remember(albumArtAccent, primary) {
        (albumArtAccent ?: primary).copy(alpha = ACCENT_BAR_ALPHA)
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(MaxHeight),
    ) {
        val barCount = min(
            VisualizerState.BAND_COUNT,
            ((size.width + gapPx) / (minBarWidthPx + gapPx)).toInt().coerceAtLeast(1),
        )

        val totalGapWidth = (barCount - 1) * gapPx
        val barWidth = (size.width - totalGapWidth) / barCount
        val topBarCornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)

        for (i in 0 until barCount) {
            // If fewer bars than bands, average adjacent bands into each bar.
            val barMagnitude = if (barCount < VisualizerState.BAND_COUNT) {
                val bandsPerBar = VisualizerState.BAND_COUNT.toFloat() / barCount
                val startBand = (i * bandsPerBar).toInt()
                val endBand = min(((i + 1) * bandsPerBar).toInt(), VisualizerState.BAND_COUNT)
                var sum = 0f
                for (b in startBand until endBand) sum += animatedMagnitudes[b]
                if (endBand > startBand) sum / (endBand - startBand) else 0f
            } else {
                animatedMagnitudes[i]
            }
            val barHeight = barMagnitude * size.height
            val x = i * (barWidth + gapPx)
            val y = size.height - barHeight

            drawPath(
                Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(
                                offset = Offset(x, y),
                                size = Size(barWidth, barHeight),
                            ),
                            topLeft = topBarCornerRadius,
                            topRight = topBarCornerRadius,
                        ),
                    )
                },
                color = barColor,
            )
        }
    }
}
