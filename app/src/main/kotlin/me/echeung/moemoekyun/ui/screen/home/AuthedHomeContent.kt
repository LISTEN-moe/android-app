package me.echeung.moemoekyun.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.domain.user.model.DomainUser
import me.echeung.moemoekyun.ui.common.SearchTextInput
import me.echeung.moemoekyun.ui.common.SongsListActions
import me.echeung.moemoekyun.ui.common.songsItems
import me.echeung.moemoekyun.util.SortType

@Composable
fun AuthedHomeContent(
    user: DomainUser,
    onClickLogOut: () -> Unit,
    favorites: List<DomainSong>?,
    query: String?,
    onQueryChange: (String) -> Unit,
    sortType: SortType,
    onSortBy: (SortType) -> Unit,
    sortDescending: Boolean,
    onSortDescending: (Boolean) -> Unit,
    requestRandomSong: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
    ) {
        item {
            UserInfo(
                user = user,
                onClickLogOut = onClickLogOut,
            )
        }

        item {
            FavoritesToolbar(
                query = query,
                onQueryChange = onQueryChange,
                sortType = sortType,
                onSortBy = onSortBy,
                sortDescending = sortDescending,
                onSortDescending = onSortDescending,
                requestRandomSong = requestRandomSong,
            )
        }

        if (favorites != null) {
            songsItems(
                songs = favorites,
            )
        }

        item {
            Spacer(modifier = Modifier.height(PlayerPeekHeight))
        }
    }
}

private val UserAvatarModifier = Modifier
    .aspectRatio(1f)
    .clip(RoundedCornerShape(8.dp))
    .fillMaxHeight()

@Composable
private fun UserInfo(
    user: DomainUser,
    onClickLogOut: () -> Unit,
) {
    var showLogoutConfirmation by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(8.dp),
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
            model = user.bannerUrl,
            contentScale = ContentScale.Crop,
            contentDescription = null,
        )

        Row(
            modifier = Modifier
                .height(96.dp)
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (user.avatarUrl == null) {
                Image(
                    modifier = UserAvatarModifier,
                    painter = painterResource(R.drawable.default_avatar),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                )
            } else {
                AsyncImage(
                    modifier = UserAvatarModifier,
                    model = user.avatarUrl,
                    placeholder = painterResource(R.drawable.default_avatar),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                )
            }

            Text(
                modifier = Modifier.weight(1f),
                text = user.username,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            IconButton(onClick = { showLogoutConfirmation = true }) {
                Icon(
                    imageVector = Icons.Outlined.Logout,
                    contentDescription = stringResource(R.string.logout),
                )
            }
        }
    }

    if (showLogoutConfirmation) {
        val dismissDialog = { showLogoutConfirmation = false }
        AlertDialog(
            onDismissRequest = dismissDialog,
            text = { Text(stringResource(R.string.logout_confirmation)) },
            confirmButton = {
                TextButton(onClick = onClickLogOut) {
                    Text(stringResource(R.string.logout))
                }
            },
            dismissButton = {
                TextButton(onClick = dismissDialog) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun FavoritesToolbar(
    query: String?,
    onQueryChange: (String) -> Unit,
    sortType: SortType,
    onSortBy: (SortType) -> Unit,
    sortDescending: Boolean,
    onSortDescending: (Boolean) -> Unit,
    requestRandomSong: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SearchTextInput(
            modifier = Modifier.weight(1f),
            query = query,
            onQueryChange = onQueryChange,
        )

        SongsListActions(
            sortType = sortType,
            onSortBy = onSortBy,
            sortDescending = sortDescending,
            onSortDescending = onSortDescending,
            requestRandomSong = requestRandomSong,
        )
    }

    Divider()
}
