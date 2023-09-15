package me.echeung.moemoekyun.ui.screen.songs

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.ui.common.AlbumArt
import me.echeung.moemoekyun.util.ext.copyToClipboard

@Composable
fun SongDetails(
    song: DomainSong,
    actionsEnabled: Boolean,
    toggleFavorite: (Int) -> Unit,
    request: (DomainSong) -> Unit,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.height(80.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AlbumArt(
                albumArtUrl = song.albumArtUrl,
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = song.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            context.copyToClipboard(song.title, song.title)
                        },
                )

                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.bodySmall,
                    LocalContentColor provides MaterialTheme.colorScheme.secondary,
                ) {
                    Text(
                        text = song.duration.takeIf { song.durationSeconds > 0 } ?: "-",
                        maxLines = 1,
                    )
                }
            }
        }

        Section(R.string.song_artist, song.artists)
        Section(R.string.song_album, song.albums)
        Section(R.string.song_source, song.sources)

        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = { toggleFavorite(song.id) },
                enabled = actionsEnabled,
            ) {
                Text(stringResource(if (song.favorited) R.string.action_unfavorite else R.string.action_favorite))
            }

            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = { request(song) },
                enabled = actionsEnabled,
            ) {
                Text(stringResource(R.string.action_request))
            }
        }
    }
}

@Composable
private fun ColumnScope.Section(
    @StringRes heading: Int,
    value: String?,
) {
    val context = LocalContext.current

    value.orEmpty().takeIf { it.isNotBlank() }?.let {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.bodySmall,
            LocalContentColor provides MaterialTheme.colorScheme.secondary,
        ) {
            Text(
                text = stringResource(heading),
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Text(
            text = it,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    context.copyToClipboard(it, it)
                },
        )
    }
}
