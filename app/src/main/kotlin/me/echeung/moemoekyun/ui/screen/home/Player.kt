package me.echeung.moemoekyun.ui.screen.home

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.cast.MediaRouteButton
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
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.ui.common.AlbumArt
import me.echeung.moemoekyun.util.ext.copyToClipboard

val PlayerPeekHeight = 72.dp

@Composable
@OptIn(UnstableApi::class)
fun PlayerScaffold(
    radioState: RadioState,
    mediaController: MediaController?,
    accentColor: Color?,
    onClickStation: (Station) -> Unit,
    onClickHistory: () -> Unit,
    toggleFavorite: ((Int) -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(PaddingValues) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val playPauseButtonState = rememberPlayPauseButtonState(mediaController)
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Expanded,
            skipHiddenState = false,
        ),
    )

    BackHandler(
        enabled = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded,
        onBack = {
            scope.launch {
                scaffoldState.bottomSheetState.hide()
            }
        },
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            PlayerContent(
                radioState = radioState,
                playPauseButtonState = playPauseButtonState,
                accentColor = accentColor,
                onClickStation = onClickStation,
                onClickHistory = onClickHistory,
                toggleFavorite = toggleFavorite,
                onClickCollapse = {
                    scope.launch {
                        scaffoldState.bottomSheetState.hide()
                    }
                },
            )
        },
        sheetContainerColor = MaterialTheme.colorScheme.background,
        sheetMaxWidth = Dp.Unspecified,
        sheetPeekHeight = 0.dp,
        sheetShape = RoundedCornerShape(0.dp),
        sheetDragHandle = {},
        modifier = modifier,
    ) { contentPadding ->
        Box {
            content(contentPadding)

            CollapsedPlayerContent(
                radioState = radioState,
                playPauseButtonState = playPauseButtonState,
                onClick = {
                    scope.launch {
                        scaffoldState.bottomSheetState.expand()
                    }
                },
            )
        }
    }
}

@Composable
@OptIn(UnstableApi::class)
private fun PlayerContent(
    radioState: RadioState,
    playPauseButtonState: PlayPauseButtonState,
    accentColor: Color?,
    onClickStation: (Station) -> Unit,
    onClickHistory: () -> Unit,
    toggleFavorite: ((Int) -> Unit)?,
    onClickCollapse: () -> Unit,
) {
    ExpandedPlayerContent(
        radioState = radioState,
        playPauseButtonState = playPauseButtonState,
        accentColor = accentColor,
        onClickCollapse = onClickCollapse,
        onClickStation = onClickStation,
        onClickHistory = onClickHistory,
        toggleFavorite = toggleFavorite,
    )
}

@OptIn(UnstableApi::class)
@Composable
private fun BoxScope.CollapsedPlayerContent(
    radioState: RadioState,
    playPauseButtonState: PlayPauseButtonState,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .height(PlayerPeekHeight)
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AlbumArt(
                albumArtUrl = radioState.albumArtUrl,
                openUrlOnClick = false,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (radioState.currentSong == null) {
                    CircularProgressIndicator()
                } else {
                    Text(
                        text = radioState.currentSong.title,
                        maxLines = 1,
                    )

                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.bodySmall,
                        LocalContentColor provides MaterialTheme.colorScheme.secondary,
                    ) {
                        Text(
                            text = radioState.currentSong.artists.orEmpty(),
                            maxLines = 1,
                        )
                    }
                }
            }

            IconButton(onClick = playPauseButtonState::onClick, enabled = playPauseButtonState.isEnabled) {
                PlayStateIcon(playPauseButtonState)
            }

            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
@OptIn(UnstableApi::class)
private fun ExpandedPlayerContent(
    radioState: RadioState,
    playPauseButtonState: PlayPauseButtonState,
    accentColor: Color?,
    onClickCollapse: () -> Unit,
    onClickStation: (Station) -> Unit,
    onClickHistory: () -> Unit,
    toggleFavorite: ((Int) -> Unit)?,
) {
    val backgroundColor = animateColorAsState(
        targetValue = accentColor ?: MaterialTheme.colorScheme.surface,
        animationSpec = tween(500, 0, LinearEasing),
        label = "playerBackground",
    )

    Surface(
        color = backgroundColor.value,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides contentColorFor(backgroundColor.value),
        ) {
            BoxWithConstraints {
                if (maxWidth < maxHeight) {
                    PortraitExpandedPlayerContent(
                        radioState = radioState,
                        playPauseButtonState = playPauseButtonState,
                        onClickCollapse = onClickCollapse,
                        onClickStation = onClickStation,
                        onClickHistory = onClickHistory,
                        toggleFavorite = toggleFavorite,
                    )
                } else {
                    LandscapeExpandedPlayerContent(
                        radioState = radioState,
                        playPauseButtonState = playPauseButtonState,
                        onClickCollapse = onClickCollapse,
                        onClickStation = onClickStation,
                        onClickHistory = onClickHistory,
                        toggleFavorite = toggleFavorite,
                    )
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun PortraitExpandedPlayerContent(
    radioState: RadioState,
    playPauseButtonState: PlayPauseButtonState,
    onClickCollapse: () -> Unit,
    onClickStation: (Station) -> Unit,
    onClickHistory: () -> Unit,
    toggleFavorite: ((Int) -> Unit)?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
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
        )

        SongInfo(
            radioState,
            playPauseButtonState,
            radioState.currentSong,
            onClickHistory,
            toggleFavorite,
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun LandscapeExpandedPlayerContent(
    radioState: RadioState,
    playPauseButtonState: PlayPauseButtonState,
    onClickCollapse: () -> Unit,
    onClickStation: (Station) -> Unit,
    onClickHistory: () -> Unit,
    toggleFavorite: ((Int) -> Unit)?,
) {
    Row(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(32.dp),
        ) {
            AlbumArt(
                albumArtUrl = radioState.albumArtUrl,
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

            SongInfo(
                radioState,
                playPauseButtonState,
                radioState.currentSong,
                onClickHistory,
                toggleFavorite,
            )
        }
    }
}

@Composable
private fun CollapseIcon(onClickCollapse: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClickCollapse)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.alpha(0.5f),
            imageVector = Icons.Outlined.ExpandMore,
            contentDescription = null,
        )

        MediaRouteButton()
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

@OptIn(UnstableApi::class)
@Composable
private fun SongInfo(
    radioState: RadioState,
    playPauseButtonState: PlayPauseButtonState,
    currentSong: DomainSong?,
    onClickHistory: () -> Unit,
    toggleFavorite: ((Int) -> Unit)?,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
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

        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClickHistory) {
                Icon(Icons.Outlined.History, contentDescription = stringResource(R.string.last_played))
            }
            FloatingActionButton(
                modifier = Modifier.size(64.dp),
                onClick = playPauseButtonState::onClick,
            ) {
                PlayStateIcon(playPauseButtonState)
            }
            IconButton(
                onClick = { currentSong?.let { toggleFavorite?.invoke(it.id) } },
                enabled = currentSong != null && toggleFavorite != null,
            ) {
                if (currentSong?.favorited == true) {
                    Icon(
                        Icons.Outlined.Star,
                        contentDescription = stringResource(R.string.action_unfavorite),
                    )
                } else {
                    Icon(
                        Icons.Outlined.StarOutline,
                        contentDescription = stringResource(R.string.action_favorite),
                    )
                }
            }
        }

        Row(
            modifier = Modifier.alpha(0.7f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodySmall,
            ) {
                Icon(Icons.Outlined.Headphones, contentDescription = null)

                Text(radioState.listeners.toString())

                radioState.requester?.let {
                    Text("•")
                    Icon(Icons.Outlined.Person, contentDescription = null)
                    Text(it)
                }
            }
        }
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
                Icons.Outlined.PlayArrow,
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
