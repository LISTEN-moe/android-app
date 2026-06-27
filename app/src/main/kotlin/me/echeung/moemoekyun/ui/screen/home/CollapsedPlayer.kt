package me.echeung.moemoekyun.ui.screen.home

import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import me.echeung.moemoekyun.domain.radio.RadioState
import me.echeung.moemoekyun.ui.common.AlbumArt
import me.echeung.moemoekyun.ui.common.CollapsedSongProgressBar
import me.echeung.moemoekyun.ui.common.rememberSongProgress
import kotlin.time.ExperimentalTime

@OptIn(UnstableApi::class, ExperimentalTime::class)
@Composable
internal fun CollapsedPlayerContent(
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
