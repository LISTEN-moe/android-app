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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import me.echeung.moemoekyun.service.VisualizerState
import kotlin.math.min

private val MaxHeight = 120.dp
private val MinBarWidth = 12.dp
private val BarGap = 2.dp
private val BarCornerRadius = 2.dp
private const val BAR_ALPHA = 0.12f

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

    val barColor = MaterialTheme.colorScheme.onBackground.copy(alpha = BAR_ALPHA)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(MaxHeight),
    ) {
        val barCount = min(
            VisualizerState.BAND_COUNT,
            ((size.width + gapPx) / (minBarWidthPx + gapPx)).toInt().coerceAtLeast(1),
        )

        // If fewer bars than bands, merge adjacent bands
        val mergedMagnitudes = if (barCount < VisualizerState.BAND_COUNT) {
            FloatArray(barCount) { i ->
                val bandsPerBar = VisualizerState.BAND_COUNT.toFloat() / barCount
                val startBand = (i * bandsPerBar).toInt()
                val endBand = min(((i + 1) * bandsPerBar).toInt(), VisualizerState.BAND_COUNT)
                var sum = 0f
                for (b in startBand until endBand) sum += animatedMagnitudes[b]
                if (endBand > startBand) sum / (endBand - startBand) else 0f
            }
        } else {
            FloatArray(barCount) { i -> animatedMagnitudes[i] }
        }

        val totalGapWidth = (barCount - 1) * gapPx
        val barWidth = (size.width - totalGapWidth) / barCount

        for (i in 0 until barCount) {
            val barHeight = mergedMagnitudes[i] * size.height
            val x = i * (barWidth + gapPx)
            val y = size.height - barHeight

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
            )
        }
    }
}
