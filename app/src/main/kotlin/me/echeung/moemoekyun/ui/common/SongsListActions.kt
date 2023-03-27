package me.echeung.moemoekyun.ui.common

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.util.SortType

@Composable
fun RowScope.SongsListActions(
    sortType: SortType,
    onSortBy: (SortType) -> Unit,
    sortDescending: Boolean,
    onSortDescending: (Boolean) -> Unit,
    requestRandomSong: () -> Unit,
) {
    var showSortMenu by remember { mutableStateOf(false) }

    IconButton(onClick = { showSortMenu = !showSortMenu }) {
        Icon(
            imageVector = Icons.Outlined.Sort,
            contentDescription = stringResource(R.string.sort),
        )
    }
    DropdownMenu(
        expanded = showSortMenu,
        onDismissRequest = { showSortMenu = false },
    ) {
        DropdownMenuItem(
            onClick = {
                onSortBy(SortType.TITLE)
                showSortMenu = false
            },
            text = { Text(stringResource(R.string.sort_title)) },
            trailingIcon = {
                RadioIcon(checked = sortType == SortType.TITLE)
            },
        )
        DropdownMenuItem(
            onClick = {
                onSortBy(SortType.ARTIST)
                showSortMenu = false
            },
            text = { Text(stringResource(R.string.sort_artist)) },
            trailingIcon = {
                RadioIcon(checked = sortType == SortType.ARTIST)
            },
        )
        DropdownMenuItem(
            onClick = {
                onSortDescending(!sortDescending)
                showSortMenu = false
            },
            text = { Text(stringResource(R.string.sort_desc)) },
            trailingIcon = {
                CheckboxIcon(checked = sortDescending)
            },
        )
    }

    IconButton(onClick = requestRandomSong) {
        Icon(
            imageVector = Icons.Outlined.Shuffle,
            contentDescription = stringResource(R.string.random_request),
        )
    }
}
