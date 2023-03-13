package me.echeung.moemoekyun.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldDefaults
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.stream.Stream
import me.echeung.moemoekyun.domain.radio.RadioState
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.ui.common.AlbumArt

val PlayerPeekHeight = BottomSheetScaffoldDefaults.SheetPeekHeight

@Composable
fun PlayerScaffold(
    radioState: RadioState,
    accentColor: Color?,
    onClickHistory: () -> Unit,
    togglePlayState: () -> Unit,
    toggleFavorite: (Int) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(BottomSheetValue.Expanded),
    )
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = { PlayerContent(scaffoldState, radioState, accentColor, onClickHistory, togglePlayState, toggleFavorite) },
        sheetBackgroundColor = MaterialTheme.colorScheme.background,
    ) { contentPadding ->
        content(contentPadding)
    }
}

@Composable
private fun PlayerContent(
    scaffoldState: BottomSheetScaffoldState,
    radioState: RadioState,
    accentColor: Color?,
    onClickHistory: () -> Unit,
    togglePlayState: () -> Unit,
    toggleFavorite: (Int) -> Unit,
) {
    Box {
        ExpandedPlayerContent(
            radioState,
            accentColor,
            onClickHistory,
            togglePlayState,
            toggleFavorite,
        )

        CollapsedPlayerContent(
            scaffoldState,
            radioState,
            togglePlayState,
        )
    }
}

@Composable
private fun CollapsedPlayerContent(
    scaffoldState: BottomSheetScaffoldState,
    radioState: RadioState,
    togglePlayState: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    val alpha = when {
        scaffoldState.bottomSheetState.isExpanded -> 0f
        scaffoldState.bottomSheetState.isCollapsed -> 1f
        else -> scaffoldState.bottomSheetState.progress
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { scope.launch { scaffoldState.bottomSheetState.expand() } }
            .height(PlayerPeekHeight)
            .alpha(alpha),
    ) {
        Row(
            modifier = Modifier.padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AlbumArt(
                albumArtUrl = radioState.currentSong?.albumArtUrl,
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

            IconButton(onClick = togglePlayState) {
                if (radioState.streamState == Stream.State.PLAY) {
                    Icon(
                        Icons.Outlined.Pause,
                        contentDescription = stringResource(R.string.action_pause),
                    )
                } else {
                    Icon(
                        Icons.Outlined.PlayArrow,
                        contentDescription = stringResource(R.string.action_play),
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandedPlayerContent(
    radioState: RadioState,
    accentColor: Color?,
    onClickHistory: () -> Unit,
    togglePlayState: () -> Unit,
    toggleFavorite: (Int) -> Unit,
) {
    Surface(
        color = accentColor ?: MaterialTheme.colorScheme.surface,
    ) {
        Icon(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .alpha(0.5f),
            imageVector = Icons.Outlined.ExpandMore,
            contentDescription = null,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AlbumArt(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .weight(2f),
                albumArtUrl = radioState.currentSong?.albumArtUrl,
            )

            SongInfo(
                modifier = Modifier.weight(1f),
                radioState,
                radioState.currentSong,
                onClickHistory,
                togglePlayState,
                toggleFavorite,
            )
        }
    }
}

@Composable
private fun SongInfo(
    modifier: Modifier,
    radioState: RadioState,
    currentSong: DomainSong?,
    onClickHistory: () -> Unit,
    togglePlayState: () -> Unit,
    toggleFavorite: (Int) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (currentSong == null) {
            CircularProgressIndicator()
        } else {
            Text(
                text = currentSong.title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            currentSong.artists?.let {
                Text(
                    text = it,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // TODO: show song progress
        // Text("${radioState.startTime} / ${currentSong?.duration}")

        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            IconButton(onClick = onClickHistory) {
                Icon(Icons.Outlined.History, contentDescription = stringResource(R.string.last_played))
            }
            FloatingActionButton(onClick = togglePlayState) {
                if (radioState.streamState == Stream.State.PLAY) {
                    Icon(
                        Icons.Outlined.Pause,
                        contentDescription = stringResource(R.string.action_pause),
                    )
                } else {
                    Icon(
                        Icons.Outlined.PlayArrow,
                        contentDescription = stringResource(R.string.action_play),
                    )
                }
            }
            IconButton(
                onClick = { currentSong?.let { toggleFavorite(it.id) } },
                enabled = currentSong != null,
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
