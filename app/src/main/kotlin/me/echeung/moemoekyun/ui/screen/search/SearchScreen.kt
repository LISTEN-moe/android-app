package me.echeung.moemoekyun.ui.screen.search

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getScreenModel
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.common.LoadingScreen
import me.echeung.moemoekyun.ui.common.SongsListActions
import me.echeung.moemoekyun.ui.common.UpButton
import me.echeung.moemoekyun.ui.common.songsItems

object SearchScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel = getScreenModel<SearchScreenModel>()
        val state by screenModel.state.collectAsState()

        var active by rememberSaveable { mutableStateOf(false) }

        val horizontalPadding by animateDpAsState(
            targetValue = if (active) 0.dp else 16.dp,
            label = "searchBarPadding",
        )

        Scaffold(
            topBar = {
                SearchBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding),
                    query = state.searchQuery ?: "",
                    onQueryChange = screenModel::search,
                    onSearch = { active = false },
                    active = active,
                    onActiveChange = {
                        active = it
                    },
                    placeholder = { Text(stringResource(R.string.search)) },
                    leadingIcon = { UpButton() },
                    trailingIcon = {
                        Row {
                            SongsListActions(
                                selectedSortType = state.sortType,
                                onSortBy = screenModel::sortBy,
                                sortDescending = state.sortDescending,
                                onSortDescending = screenModel::sortDescending,
                                requestRandomSong = screenModel::requestRandomSong,
                            )
                        }
                    },
                ) {
                    LazyColumn {
                        songsItems(
                            songs = state.filteredSongs,
                            showFavoriteIcons = true,
                        )
                    }
                }
            },
        ) { contentPadding ->
            if (state.songs == null) {
                LoadingScreen()
                return@Scaffold
            }

            LazyColumn(
                contentPadding = contentPadding,
            ) {
                songsItems(
                    songs = state.filteredSongs,
                    showFavoriteIcons = true,
                )
            }
        }
    }
}
