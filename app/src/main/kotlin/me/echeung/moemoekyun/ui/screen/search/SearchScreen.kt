package me.echeung.moemoekyun.ui.screen.search

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getScreenModel
import me.echeung.moemoekyun.ui.common.LoadingScreen
import me.echeung.moemoekyun.ui.common.SearchTextInput
import me.echeung.moemoekyun.ui.common.SongsListActions
import me.echeung.moemoekyun.ui.common.Toolbar
import me.echeung.moemoekyun.ui.common.songsItems

class SearchScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel = getScreenModel<SearchScreenModel>()
        val state by screenModel.state.collectAsState()

        Scaffold(
            topBar = {
                Toolbar(
                    title = {
                        SearchTextInput(
                            modifier = Modifier.fillMaxWidth(),
                            query = state.searchQuery,
                            onQueryChange = screenModel::search,
                        )
                    },
                    showUpButton = true,
                    actions = {
                        SongsListActions(
                            selectedSortType = state.sortType,
                            onSortBy = screenModel::sortBy,
                            sortDescending = state.sortDescending,
                            onSortDescending = screenModel::sortDescending,
                            requestRandomSong = screenModel::requestRandomSong,
                        )
                    },
                )
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
                    songs = state.filteredSongs!!,
                    showFavoriteIcons = true,
                )
            }
        }
    }
}
