package me.echeung.moemoekyun.ui.screen.search

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
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
        val navigator = LocalNavigator.currentOrThrow

        val onExpandedChange = { expanded: Boolean ->
            if (!expanded) {
                navigator.pop()
            }
        }

        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = state.searchQuery ?: "",
                    onQueryChange = screenModel::search,
                    onSearch = {},
                    expanded = true,
                    onExpandedChange = onExpandedChange,
                    enabled = true,
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
                )
            },
            expanded = true,
            onExpandedChange = onExpandedChange,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.songs == null) {
                LoadingScreen()
                return@SearchBar
            }

            LazyColumn(
                modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom)),
            ) {
                songsItems(
                    songs = state.filteredSongs,
                    showFavoriteIcons = true,
                )
            }
        }
    }
}
