package me.echeung.moemoekyun.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList
import me.echeung.moemoekyun.domain.songs.model.DomainSong

fun LazyListScope.songsItems(
    songs: ImmutableList<DomainSong>?,
    showFavoriteIcons: Boolean = false,
    onShowSongs: (List<DomainSong>) -> Unit = {},
) = items(
    items = songs.orEmpty(),
    key = { it.id },
) {
    ListItem(
        modifier = Modifier
            .clickable {
                onShowSongs(listOf(it))
            },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
        headlineContent = {
            Text(text = it.title)
        },
        supportingContent = {
            it.artists?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        },
        trailingContent = {
            if (showFavoriteIcons && it.favorited) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
    )
}
