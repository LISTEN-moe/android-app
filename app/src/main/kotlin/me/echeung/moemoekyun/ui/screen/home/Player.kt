package me.echeung.moemoekyun.ui.screen.home

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.media3.common.Player
import androidx.media3.common.listen
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util.handlePlayPauseButtonAction
import androidx.media3.common.util.Util.shouldEnablePlayPauseButton
import androidx.media3.common.util.Util.shouldShowPlayButton
import androidx.media3.session.MediaController
import kotlinx.coroutines.launch
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.api.Station
import me.echeung.moemoekyun.domain.radio.RadioState
import me.echeung.moemoekyun.service.VisualizerState
import me.echeung.moemoekyun.ui.common.AlbumArt
import me.echeung.moemoekyun.ui.common.AudioVisualizer
import me.echeung.moemoekyun.ui.common.CollapsedSongProgressBar
import me.echeung.moemoekyun.ui.common.ExpandedSongProgressBar
import me.echeung.moemoekyun.ui.common.LocalAlbumArtAccentColor
import me.echeung.moemoekyun.ui.common.rememberSongProgress
import me.echeung.moemoekyun.util.ext.copyToClipboard
import kotlin.time.ExperimentalTime

/** Visible height of the collapsed player bar (excluding the bottom system bar inset). */
val PlayerPeekHeight = 80.dp

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

    BackHandler(
        enabled = isSheetExpanded,
        onBack = { scope.launch { sheetState.partialExpand() } },
    )

    val bottomInset = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
    val expandProgress = rememberSheetExpandProgress(sheetState, PlayerPeekHeight + bottomInset)

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

/** Background opacity of the sheet when only the collapsed bar is showing. */
private const val COLLAPSED_SHEET_ALPHA = 0.88f

/**
 * Continuous 0..1 drag progress for a [SheetState] with [SheetValue.PartiallyExpanded] and
 * [SheetValue.Expanded] anchors. 0 = peek, 1 = fully expanded.
 *
 * Attach [SheetExpandProgress.measureModifier] to the sheet content so the helper can read its
 * laid-out height (needed because the partial-anchor offset is not exposed by the public API).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberSheetExpandProgress(sheetState: SheetState, peekHeight: Dp): SheetExpandProgress {
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

private class SheetExpandProgress(private val valueProvider: () -> Float, val measureModifier: Modifier) {
    val value: Float get() = valueProvider()
}

@OptIn(UnstableApi::class, ExperimentalTime::class)
@Composable
private fun CollapsedPlayerContent(
    radioState: RadioState,
    playPauseButtonState: PlayPauseButtonState,
    toggleFavorite: ((Int) -> Unit)?,
    bottomInset: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val durationSeconds = radioState.currentSong?.durationSeconds ?: 0L
    val progress = rememberSongProgress(radioState.startTime?.toEpochMilliseconds(), durationSeconds)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        if (durationSeconds > 0L) {
            CollapsedSongProgressBar(progress = progress)
        }
        HorizontalDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(PlayerPeekHeight)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(modifier = Modifier.size(56.dp)) {
                AlbumArt(
                    modifier = Modifier.fillMaxSize(),
                    albumArtUrl = radioState.albumArtUrl,
                    openUrlOnClick = false,
                    cornerRadius = 8.dp,
                )
            }

            PlayerCircularPlayPauseButton(
                playPauseButtonState = playPauseButtonState,
                size = 48.dp,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (radioState.currentSong == null) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = radioState.currentSong.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.bodySmall,
                        LocalContentColor provides MaterialTheme.colorScheme.secondary,
                    ) {
                        Text(
                            text = radioState.currentSong.artists.orEmpty(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            if (toggleFavorite != null) {
                val song = radioState.currentSong
                SongFavoriteIconButton(
                    songId = song?.id,
                    favorited = song?.favorited == true,
                    onToggleFavorite = toggleFavorite,
                )
            }
        }

        Spacer(modifier = Modifier.height(bottomInset))
    }
}

@Composable
@OptIn(UnstableApi::class)
private fun ExpandedPlayerContent(
    radioState: RadioState,
    playPauseButtonState: PlayPauseButtonState,
    visualizerState: VisualizerState,
    isVisualizerEnabled: Boolean,
    onClickCollapse: () -> Unit,
    onClickStation: (Station) -> Unit,
    onClickHistory: () -> Unit,
    toggleFavorite: ((Int) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        if (maxWidth < maxHeight) {
            PortraitExpandedPlayerContent(
                radioState = radioState,
                playPauseButtonState = playPauseButtonState,
                visualizerState = visualizerState,
                isVisualizerEnabled = isVisualizerEnabled,
                onClickCollapse = onClickCollapse,
                onClickStation = onClickStation,
                onClickHistory = onClickHistory,
                toggleFavorite = toggleFavorite,
            )
        } else {
            LandscapeExpandedPlayerContent(
                radioState = radioState,
                playPauseButtonState = playPauseButtonState,
                visualizerState = visualizerState,
                isVisualizerEnabled = isVisualizerEnabled,
                onClickCollapse = onClickCollapse,
                onClickStation = onClickStation,
                onClickHistory = onClickHistory,
                toggleFavorite = toggleFavorite,
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun PortraitExpandedPlayerContent(
    radioState: RadioState,
    playPauseButtonState: PlayPauseButtonState,
    visualizerState: VisualizerState,
    isVisualizerEnabled: Boolean,
    onClickCollapse: () -> Unit,
    onClickStation: (Station) -> Unit,
    onClickHistory: () -> Unit,
    toggleFavorite: ((Int) -> Unit)?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.systemBars.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                ),
            )
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CollapseIcon(onClickCollapse)
        StationPicker(radioState, onClickStation)

        AlbumArt(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .weight(1f),
            albumArtUrl = radioState.albumArtUrl,
            cornerRadius = 32.dp,
        )

        SongInfoWithVisualizer(
            radioState = radioState,
            playPauseButtonState = playPauseButtonState,
            visualizerState = visualizerState,
            isVisualizerEnabled = isVisualizerEnabled,
            onClickHistory = onClickHistory,
            toggleFavorite = toggleFavorite,
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun LandscapeExpandedPlayerContent(
    radioState: RadioState,
    playPauseButtonState: PlayPauseButtonState,
    visualizerState: VisualizerState,
    isVisualizerEnabled: Boolean,
    onClickCollapse: () -> Unit,
    onClickStation: (Station) -> Unit,
    onClickHistory: () -> Unit,
    toggleFavorite: ((Int) -> Unit)?,
) {
    Row(
        modifier = Modifier
            .windowInsetsPadding(
                WindowInsets.systemBars.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(32.dp),
        ) {
            AlbumArt(
                albumArtUrl = radioState.albumArtUrl,
                cornerRadius = 32.dp,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CollapseIcon(onClickCollapse)
            StationPicker(radioState, onClickStation)

            SongInfoWithVisualizer(
                radioState = radioState,
                playPauseButtonState = playPauseButtonState,
                visualizerState = visualizerState,
                isVisualizerEnabled = isVisualizerEnabled,
                onClickHistory = onClickHistory,
                toggleFavorite = toggleFavorite,
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun SongInfoWithVisualizer(
    radioState: RadioState,
    playPauseButtonState: PlayPauseButtonState,
    visualizerState: VisualizerState,
    isVisualizerEnabled: Boolean,
    onClickHistory: () -> Unit,
    toggleFavorite: ((Int) -> Unit)?,
) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier.height(IntrinsicSize.Min),
    ) {
        if (isVisualizerEnabled) {
            AudioVisualizer(state = visualizerState)
        }
        SongInfo(
            radioState,
            playPauseButtonState,
            onClickHistory,
            toggleFavorite,
        )
    }
}

@Composable
private fun CollapseIcon(onClickCollapse: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClickCollapse)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.alpha(0.5f),
            imageVector = Icons.Outlined.ExpandMore,
            contentDescription = null,
        )
    }
}

@Composable
private fun StationPicker(radioState: RadioState, onClickStation: (Station) -> Unit) {
    val colors = SegmentedButtonDefaults.colors(
        activeContainerColor = MaterialTheme.colorScheme.primary,
        inactiveContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        activeBorderColor = MaterialTheme.colorScheme.surface,
        inactiveBorderColor = MaterialTheme.colorScheme.surface,
    )

    MultiChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Station.entries.forEachIndexed { index, station ->
            SegmentedButton(
                checked = radioState.station == station,
                onCheckedChange = { onClickStation(station) },
                colors = colors,
                shape = SegmentedButtonDefaults.itemShape(
                    index,
                    Station.entries.size,
                ),
            ) {
                Text(stringResource(station.labelRes))
            }
        }
    }
}

@OptIn(UnstableApi::class, ExperimentalTime::class)
@Composable
private fun SongInfo(
    radioState: RadioState,
    playPauseButtonState: PlayPauseButtonState,
    onClickHistory: () -> Unit,
    toggleFavorite: ((Int) -> Unit)?,
) {
    val context = LocalContext.current
    val currentSong = radioState.currentSong
    val durationSeconds = currentSong?.durationSeconds ?: 0L
    val progress = rememberSongProgress(radioState.startTime?.toEpochMilliseconds(), durationSeconds)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (currentSong == null) {
            CircularProgressIndicator()
        } else {
            radioState.event?.let {
                Text(
                    text = "♫♪.ılılıll ${it.name} llılılı.♫♪",
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Text(
                text = currentSong.title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .clickable {
                        context.copyToClipboard(currentSong.title, currentSong.title)
                    },
            )

            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.titleSmall,
                LocalContentColor provides MaterialTheme.colorScheme.secondary,
            ) {
                currentSong.artists?.let {
                    Text(
                        text = it,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clickable {
                                context.copyToClipboard(currentSong.artists, currentSong.artists)
                            },
                    )
                }
            }

            ExpandedSongProgressBar(
                progress = progress,
                durationSeconds = durationSeconds,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }

        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClickHistory) {
                Icon(Icons.Outlined.History, contentDescription = stringResource(R.string.last_played))
            }
            PlayerCircularPlayPauseButton(
                playPauseButtonState = playPauseButtonState,
                size = 64.dp,
            )
            SongFavoriteIconButton(
                songId = currentSong?.id,
                favorited = currentSong?.favorited == true,
                onToggleFavorite = toggleFavorite,
            )
        }

        Row(
            modifier = Modifier.alpha(0.7f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodySmall,
                LocalContentColor provides MaterialTheme.colorScheme.secondary,
            ) {
                Icon(Icons.Outlined.Headphones, contentDescription = null)

                Text(radioState.listeners.toString())

                radioState.requester?.let {
                    Text("•")
                    Icon(Icons.Outlined.Person, contentDescription = null)
                    Text(it)
                }

                radioState.queueCount?.takeIf { it > 0 }?.let {
                    Text("•")
                    Icon(Icons.AutoMirrored.Outlined.QueueMusic, contentDescription = null)
                    Text(it.toString())
                }
            }
        }
    }
}

@Composable
private fun SongFavoriteIconButton(
    songId: Int?,
    favorited: Boolean,
    onToggleFavorite: ((Int) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = { songId?.let { onToggleFavorite?.invoke(it) } },
        enabled = songId != null && onToggleFavorite != null,
        modifier = modifier,
    ) {
        if (favorited) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = stringResource(R.string.action_unfavorite),
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = stringResource(R.string.action_favorite),
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun PlayerCircularPlayPauseButton(
    playPauseButtonState: PlayPauseButtonState,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    FloatingActionButton(
        onClick = playPauseButtonState::onClick,
        modifier = modifier.size(size),
        shape = CircleShape,
//        enabled = playPauseButtonState.isEnabled,
    ) {
        PlayStateIcon(playPauseButtonState)
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun PlayStateIcon(playPauseButtonState: PlayPauseButtonState) {
    when {
        playPauseButtonState.isBuffering -> {
            CircularProgressIndicator(
                modifier = Modifier.alpha(0.7f),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }

        playPauseButtonState.showPlay -> {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = stringResource(R.string.action_play),
            )
        }

        !playPauseButtonState.showPlay -> {
            Icon(
                Icons.Outlined.Pause,
                contentDescription = stringResource(R.string.action_pause),
            )
        }
    }
}

@UnstableApi
@Composable
fun rememberPlayPauseButtonState(player: Player?): PlayPauseButtonState {
    val playPauseButtonState = remember(player) { PlayPauseButtonState(player) }
    LaunchedEffect(player) { playPauseButtonState.observe() }
    return playPauseButtonState
}

@UnstableApi
class PlayPauseButtonState(private val player: Player?) {
    var isEnabled by mutableStateOf(shouldEnablePlayPauseButton(player))
        private set

    var showPlay by mutableStateOf(shouldShowPlayButton(player))
        private set

    var isBuffering by mutableStateOf(player?.playbackState == Player.STATE_BUFFERING)

    fun onClick() {
        handlePlayPauseButtonAction(player)
    }

    suspend fun observe(): Nothing? = player?.listen { events ->
        if (
            events.containsAny(
                Player.EVENT_PLAYBACK_STATE_CHANGED,
                Player.EVENT_PLAY_WHEN_READY_CHANGED,
                Player.EVENT_AVAILABLE_COMMANDS_CHANGED,
            )
        ) {
            showPlay = shouldShowPlayButton(this)
            isEnabled = shouldEnablePlayPauseButton(this)
            isBuffering = playbackState == Player.STATE_BUFFERING
        }
    }
}
