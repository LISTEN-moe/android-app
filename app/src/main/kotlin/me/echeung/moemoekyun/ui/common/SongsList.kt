package me.echeung.moemoekyun.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import me.echeung.moemoekyun.domain.songs.model.DomainSong

fun LazyListScope.songsItems(
    songs: ImmutableList<DomainSong>?,
    onShowSongs: (ImmutableList<DomainSong>) -> Unit = {},
) = items(
    items = songs.orEmpty(),
    key = { it.id },
) {
    ListItem(
        modifier = Modifier
            .clickable {
                onShowSongs(persistentListOf(it))
            },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
        headlineContent = {
            Text(text = it.title)
        },
        supportingContent = {
            it.artists?.let { artists ->
                Text(
                    text = artists,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        },
    )
}
