package me.echeung.moemoekyun.ui.screen.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * Continuous 0..1 drag progress for a [SheetState] with [SheetValue.PartiallyExpanded] and
 * [SheetValue.Expanded] anchors. 0 = peek, 1 = fully expanded.
 *
 * Attach [SheetExpandProgress.measureModifier] to the sheet content so the helper can read its
 * laid-out height (needed because the partial-anchor offset is not exposed by the public API).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun rememberSheetExpandProgress(sheetState: SheetState, peekHeight: Dp): SheetExpandProgress {
    val peekHeightPx = with(LocalDensity.current) { peekHeight.toPx() }
    var sheetContentHeightPx by remember { mutableIntStateOf(0) }
    val value by remember {
        derivedStateOf {
            val draggableRange = sheetContentHeightPx - peekHeightPx
            if (draggableRange <= 0f) {
                if (sheetState.targetValue == SheetValue.Expanded) 1f else 0f
            } else {
                val offset = runCatching { sheetState.requireOffset() }.getOrNull() ?: return@derivedStateOf 0f
                (1f - offset / draggableRange).coerceIn(0f, 1f)
            }
        }
    }
    return remember(sheetState) {
        SheetExpandProgress(
            valueProvider = { value },
            measureModifier = Modifier.onSizeChanged { sheetContentHeightPx = it.height },
        )
    }
}

internal class SheetExpandProgress(private val valueProvider: () -> Float, val measureModifier: Modifier) {
    val value: Float get() = valueProvider()
}
