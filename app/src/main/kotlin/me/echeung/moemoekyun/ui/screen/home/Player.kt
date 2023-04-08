package me.echeung.moemoekyun.ui.screen.home

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
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
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.api.Station
import me.echeung.moemoekyun.client.stream.Stream
import me.echeung.moemoekyun.domain.radio.RadioState
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.ui.common.AlbumArt
import me.echeung.moemoekyun.ui.common.SegmentedButtons

val PlayerPeekHeight = 72.dp

@Composable
fun PlayerScaffold(
    radioState: RadioState,
    accentColor: Color?,
    onClickStation: (Station) -> Unit,
    onClickHistory: () -> Unit,
    togglePlayState: () -> Unit,
    toggleFavorite: (Int) -> Unit,
    content: @Composable BoxScope.(PaddingValues) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(BottomSheetValue.Expanded),
    )

    BackHandler(
        enabled = scaffoldState.bottomSheetState.isExpanded,
        onBack = {
            scope.launch {
                scaffoldState.bottomSheetState.collapse()
            }
        },
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            PlayerContent(
                radioState = radioState,
                accentColor = accentColor,
                onClickStation = onClickStation,
                onClickHistory = onClickHistory,
                togglePlayState = togglePlayState,
                toggleFavorite = toggleFavorite,
                onClickCollapse = {
                    scope.launch {
                        scaffoldState.bottomSheetState.collapse()
                    }
                },
            )
        },
        sheetBackgroundColor = MaterialTheme.colorScheme.background,
        sheetPeekHeight = 0.dp,
    ) { contentPadding ->
        Box {
            content(contentPadding)

            CollapsedPlayerContent(
                radioState = radioState,
                togglePlayState = togglePlayState,
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
private fun PlayerContent(
    radioState: RadioState,
    accentColor: Color?,
    onClickStation: (Station) -> Unit,
    onClickHistory: () -> Unit,
    togglePlayState: () -> Unit,
    toggleFavorite: (Int) -> Unit,
    onClickCollapse: () -> Unit,
) {
    ExpandedPlayerContent(
        radioState = radioState,
        accentColor = accentColor,
        onClickCollapse = onClickCollapse,
        onClickStation = onClickStation,
        onClickHistory = onClickHistory,
        togglePlayState = togglePlayState,
        toggleFavorite = toggleFavorite,
    )
}

@Composable
private fun BoxScope.CollapsedPlayerContent(
    radioState: RadioState,
    togglePlayState: () -> Unit,
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

            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
private fun ExpandedPlayerContent(
    radioState: RadioState,
    accentColor: Color?,
    onClickCollapse: () -> Unit,
    onClickStation: (Station) -> Unit,
    onClickHistory: () -> Unit,
    togglePlayState: () -> Unit,
    toggleFavorite: (Int) -> Unit,
) {
    val backgroundColor = animateColorAsState(
        targetValue = accentColor ?: MaterialTheme.colorScheme.surface,
        animationSpec = tween(500, 0, LinearEasing),
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
                        onClickCollapse = onClickCollapse,
                        onClickStation = onClickStation,
                        onClickHistory = onClickHistory,
                        togglePlayState = togglePlayState,
                        toggleFavorite = toggleFavorite,
                    )
                } else {
                    LandscapeExpandedPlayerContent(
                        radioState = radioState,
                        onClickCollapse = onClickCollapse,
                        onClickStation = onClickStation,
                        onClickHistory = onClickHistory,
                        togglePlayState = togglePlayState,
                        toggleFavorite = toggleFavorite,
                    )
                }
            }
        }
    }
}

@Composable
private fun PortraitExpandedPlayerContent(
    radioState: RadioState,
    onClickCollapse: () -> Unit,
    onClickStation: (Station) -> Unit,
    onClickHistory: () -> Unit,
    togglePlayState: () -> Unit,
    toggleFavorite: (Int) -> Unit,
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
            albumArtUrl = radioState.currentSong?.albumArtUrl,
        )

        SongInfo(
            radioState,
            radioState.currentSong,
            onClickHistory,
            togglePlayState,
            toggleFavorite,
        )
    }
}

@Composable
private fun LandscapeExpandedPlayerContent(
    radioState: RadioState,
    onClickCollapse: () -> Unit,
    onClickStation: (Station) -> Unit,
    onClickHistory: () -> Unit,
    togglePlayState: () -> Unit,
    toggleFavorite: (Int) -> Unit,
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
                albumArtUrl = radioState.currentSong?.albumArtUrl,
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
                radioState.currentSong,
                onClickHistory,
                togglePlayState,
                toggleFavorite,
            )
        }
    }
}

@Composable
private fun CollapseIcon(
    onClickCollapse: () -> Unit,
) {
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
private fun StationPicker(
    radioState: RadioState,
    onClickStation: (Station) -> Unit,
) {
    SegmentedButtons(
        entries = Station.values().map { stringResource(it.labelRes) },
        selectedIndex = Station.values().indexOf(radioState.station),
        onClick = { index -> onClickStation(Station.values()[index]) },
    )
}

@Composable
private fun SongInfo(
    radioState: RadioState,
    currentSong: DomainSong?,
    onClickHistory: () -> Unit,
    togglePlayState: () -> Unit,
    toggleFavorite: (Int) -> Unit,
) {
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
