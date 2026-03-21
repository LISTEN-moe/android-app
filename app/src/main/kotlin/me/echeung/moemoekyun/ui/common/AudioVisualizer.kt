package me.echeung.moemoekyun.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
fun AudioVisualizer(state: VisualizerState, accentColor: Color, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val minBarWidthPx = with(density) { MinBarWidth.toPx() }
    val gapPx = with(density) { BarGap.toPx() }
    val cornerRadiusPx = with(density) { BarCornerRadius.toPx() }

    val barCount = min(
        VisualizerState.BAND_COUNT,
        ((state.magnitudes.size * minBarWidthPx + gapPx) / (minBarWidthPx + gapPx)).toInt()
            .coerceAtLeast(1),
    )

    // Animate each bar independently with spring physics
    val animatedMagnitudes = Array(barCount) { i ->
        // If fewer bars than bands, merge adjacent bands
        val magnitude = if (barCount < state.magnitudes.size) {
            val bandsPerBar = state.magnitudes.size.toFloat() / barCount
            val startBand = (i * bandsPerBar).toInt()
            val endBand = min(((i + 1) * bandsPerBar).toInt(), state.magnitudes.size)
            var sum = 0f
            for (b in startBand until endBand) sum += state.magnitudes[b]
            if (endBand > startBand) sum / (endBand - startBand) else 0f
        } else {
            state.magnitudes.getOrElse(i) { 0f }
        }

        val animated by animateFloatAsState(
            targetValue = magnitude,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow,
            ),
            label = "bar_$i",
        )
        animated
    }

    val barColor = accentColor.copy(alpha = BAR_ALPHA)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(MaxHeight),
    ) {
        val actualBarCount = min(
            barCount,
            ((size.width + gapPx) / (minBarWidthPx + gapPx)).toInt().coerceAtLeast(1),
        )
        val totalGapWidth = (actualBarCount - 1) * gapPx
        val barWidth = (size.width - totalGapWidth) / actualBarCount

        for (i in 0 until actualBarCount) {
            val barHeight = animatedMagnitudes.getOrElse(i) { 0f } * size.height
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
