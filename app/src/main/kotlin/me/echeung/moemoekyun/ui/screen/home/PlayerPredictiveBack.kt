package me.echeung.moemoekyun.ui.screen.home

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Wires a predictive back gesture to slide the sheet down from its fully-expanded position toward
 * the peek. Returns a [Modifier] to apply to the sheet content so the visual translation tracks the
 * gesture in real time. On gesture completion, [onCommit] is invoked (typically to collapse the
 * sheet for real); on cancellation, the sheet slides back to fully expanded.
 */
@Composable
internal fun rememberPredictiveBackSlideDown(
    enabled: Boolean,
    expandProgress: SheetExpandProgress,
    onCommit: suspend () -> Unit,
): Modifier {
    val animatable = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    PredictiveBackHandler(enabled = enabled) { events ->
        try {
            events.collect { event ->
                animatable.snapTo(event.progress)
            }
            // Release our translation in parallel with the sheet's own collapse animation so the
            // sheet stays put visually as control hands off. Snapping to 0 before the sheet has
            // caught up would flash the sheet back to its fully-expanded position for a frame.
            scope.launch { animatable.animateTo(0f) }
            onCommit()
        } catch (e: CancellationException) {
            scope.launch { animatable.animateTo(0f) }
            throw e
        }
    }

    return Modifier.graphicsLayer {
        translationY = animatable.value * expandProgress.draggableRangePx
    }
}
