package me.echeung.moemoekyun.ui.screen.home

import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.api.Station
import me.echeung.moemoekyun.domain.radio.RadioState
import me.echeung.moemoekyun.service.VisualizerState
import me.echeung.moemoekyun.ui.common.AlbumArt
import me.echeung.moemoekyun.ui.common.AudioVisualizer
import me.echeung.moemoekyun.ui.common.ExpandedSongProgressBar
import me.echeung.moemoekyun.ui.common.rememberSongProgress
import me.echeung.moemoekyun.util.ext.copyToClipboard
import kotlin.time.ExperimentalTime

@Composable
@OptIn(UnstableApi::class)
internal fun ExpandedPlayerContent(
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
