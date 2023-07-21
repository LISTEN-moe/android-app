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
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.ui.screen.songs.SongsScreen

fun LazyListScope.songsItems(
    songs: List<DomainSong>,
    showFavoriteIcons: Boolean = false,
) = items(
    items = songs,
    key = { it.id },
) {
    val bottomSheetNavigator = LocalBottomSheetNavigator.current

    ListItem(
        modifier = Modifier
            .clickable {
                bottomSheetNavigator.show(
                    SongsScreen(songs = listOf(it)),
                )
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
