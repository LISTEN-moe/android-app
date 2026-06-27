package me.echeung.moemoekyun.ui.screen.home

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import kotlinx.coroutines.launch
import me.echeung.moemoekyun.client.api.Station
import me.echeung.moemoekyun.domain.radio.RadioState
import me.echeung.moemoekyun.service.VisualizerState
import me.echeung.moemoekyun.ui.common.LocalAlbumArtAccentColor

/** Visible height of the collapsed player bar (excluding the bottom system bar inset). */
val PlayerPeekHeight = 80.dp

/** Background opacity of the sheet when only the collapsed bar is showing. */
private const val COLLAPSED_SHEET_ALPHA = 0.88f

@Composable
@OptIn(UnstableApi::class)
fun PlayerScaffold(
    radioState: RadioState,
    mediaController: MediaController?,
    accentColor: Color?,
    visualizerState: VisualizerState,
    isVisualizerEnabled: Boolean,
    onSetVisualizerActive: (Boolean) -> Unit,
    onClickStation: (Station) -> Unit,
    onClickHistory: () -> Unit,
    toggleFavorite: ((Int) -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val playPauseButtonState = rememberPlayPauseButtonState(mediaController)
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
        ),
    )
    val sheetState = scaffoldState.bottomSheetState

    val isSheetExpanded = sheetState.currentValue == SheetValue.Expanded
    val isPlaying = !playPauseButtonState.showPlay

    SideEffect {
        onSetVisualizerActive(isVisualizerEnabled && isPlaying && isSheetExpanded)
    }

    val bottomInset = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
    val expandProgress = rememberSheetExpandProgress(sheetState, PlayerPeekHeight + bottomInset)
    val predictiveBackModifier = rememberPredictiveBackSlideDown(
        enabled = isSheetExpanded,
        expandProgress = expandProgress,
        onCommit = { sheetState.partialExpand() },
    )

    val surfaceColor = MaterialTheme.colorScheme.surface
    val sheetBackground = surfaceColor.copy(alpha = lerp(COLLAPSED_SHEET_ALPHA, 1f, expandProgress.value))

    CompositionLocalProvider(
        LocalAlbumArtAccentColor provides accentColor,
    ) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(predictiveBackModifier)
                        .background(sheetBackground)
                        .then(expandProgress.measureModifier),
                ) {
                    ExpandedPlayerContent(
                        radioState = radioState,
                        playPauseButtonState = playPauseButtonState,
                        visualizerState = visualizerState,
                        isVisualizerEnabled = isVisualizerEnabled,
                        onClickStation = onClickStation,
                        onClickHistory = onClickHistory,
                        toggleFavorite = toggleFavorite,
                        onClickCollapse = { scope.launch { sheetState.partialExpand() } },
                        modifier = Modifier.alpha(expandProgress.value),
                    )
                    CollapsedPlayerContent(
                        radioState = radioState,
                        playPauseButtonState = playPauseButtonState,
                        toggleFavorite = toggleFavorite,
                        bottomInset = bottomInset,
                        onClick = { scope.launch { sheetState.expand() } },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .alpha(1f - expandProgress.value),
                    )
                }
            },
            sheetContainerColor = Color.Transparent,
            sheetContentColor = contentColorFor(surfaceColor),
            sheetMaxWidth = Dp.Unspecified,
            sheetPeekHeight = PlayerPeekHeight + bottomInset,
            sheetShape = RoundedCornerShape(0.dp),
            sheetShadowElevation = 0.dp,
            sheetDragHandle = {},
            modifier = modifier,
            content = content,
        )
    }
}
