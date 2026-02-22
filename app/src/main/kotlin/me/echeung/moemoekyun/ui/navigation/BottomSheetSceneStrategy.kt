package me.echeung.moemoekyun.ui.navigation

import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import me.echeung.moemoekyun.ui.navigation.BottomSheetSceneStrategy.Companion.bottomSheet

/** An [OverlayScene] that renders an [entry] within a [ModalBottomSheet]. */
internal class BottomSheetScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val entry: NavEntry<T>,
    private val modalBottomSheetProperties: ModalBottomSheetProperties,
    private val onBack: () -> Unit,
) : OverlayScene<T> {

    override val entries: List<NavEntry<T>> = listOf(entry)

    override val content: @Composable (() -> Unit) = {
        ModalBottomSheet(
            onDismissRequest = onBack,
            properties = modalBottomSheetProperties,
        ) {
            entry.Content()
        }
    }
}

/**
 * A [SceneStrategy] that displays entries that have added [bottomSheet] to their [NavEntry.metadata]
 * within a [ModalBottomSheet] instance.
 *
 * This strategy should always be added before any non-overlay scene strategies.
 */
class BottomSheetSceneStrategy<T : Any> : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastEntry = entries.lastOrNull()
        val bottomSheetProperties =
            lastEntry?.metadata?.get(BOTTOM_SHEET_KEY) as? ModalBottomSheetProperties
        return bottomSheetProperties?.let { properties ->
            val previousEntries = entries.dropLast(1)
            BottomSheetScene(
                key = lastEntry.contentKey,
                previousEntries = previousEntries,
                overlaidEntries = previousEntries,
                entry = lastEntry,
                modalBottomSheetProperties = properties,
                onBack = onBack,
            )
        }
    }

    companion object {
        /**
         * Marks a [NavEntry] to be displayed within a [ModalBottomSheet].
         *
         * @param modalBottomSheetProperties properties passed to the [ModalBottomSheet].
         */
        fun bottomSheet(
            modalBottomSheetProperties: ModalBottomSheetProperties = ModalBottomSheetProperties(),
        ): Map<String, Any> = mapOf(BOTTOM_SHEET_KEY to modalBottomSheetProperties)

        private const val BOTTOM_SHEET_KEY = "bottomsheet"
    }
}
