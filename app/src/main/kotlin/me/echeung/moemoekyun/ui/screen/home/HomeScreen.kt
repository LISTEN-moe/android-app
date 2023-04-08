package me.echeung.moemoekyun.ui.screen.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.common.BackgroundBox
import me.echeung.moemoekyun.ui.screen.about.AboutScreen
import me.echeung.moemoekyun.ui.screen.search.SearchScreen
import me.echeung.moemoekyun.ui.screen.settings.SettingsScreen
import me.echeung.moemoekyun.ui.screen.songs.SongsScreen

class HomeScreen : Screen {

    @Composable
    override fun Content() {
        val bottomSheetNavigator = LocalBottomSheetNavigator.current

        val screenModel = getScreenModel<HomeScreenModel>()
        val state by screenModel.state.collectAsState()

        val radioState by screenModel.radioState.collectAsState()
        val isAuthenticated = state.user != null

        PlayerScaffold(
            radioState = radioState,
            accentColor = state.accentColor,
            onClickStation = screenModel::toggleLibrary,
            onClickHistory = {
                val historySongs = listOfNotNull(radioState.currentSong) + radioState.pastSongs
                if (historySongs.isNotEmpty()) {
                    bottomSheetNavigator.show(
                        SongsScreen(songs = historySongs),
                    )
                }
            },
            togglePlayState = { screenModel.togglePlayState() },
            toggleFavorite = { screenModel.toggleFavorite(it) },
        ) {
            Scaffold(
                topBar = { Toolbar(isAuthenticated = isAuthenticated) },
            ) { contentPadding ->
                BackgroundBox(
                    modifier = Modifier
                        .padding(contentPadding)
                        .fillMaxSize(),
                ) {
                    if (isAuthenticated) {
                        AuthedHomeContent(
                            user = state.user!!,
                            onClickLogOut = screenModel::logout,
                            favorites = state.filteredFavorites,
                            query = state.searchQuery,
                            onQueryChange = screenModel::search,
                            sortType = state.sortType,
                            onSortBy = screenModel::sortBy,
                            sortDescending = state.sortDescending,
                            onSortDescending = screenModel::sortDescending,
                            requestRandomSong = screenModel::requestRandomSong,
                        )
                    } else {
                        UnauthedHomeContent()
                    }
                }
            }
        }
    }

    @Composable
    private fun Toolbar(
        isAuthenticated: Boolean,
    ) {
        val navigator = LocalNavigator.currentOrThrow

        TopAppBar(
            title = {},
            navigationIcon = {
                if (isAuthenticated) {
                    IconButton(onClick = { navigator.push(SearchScreen()) }) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = stringResource(R.string.search),
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = { navigator.push(AboutScreen()) }) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = stringResource(R.string.about),
                    )
                }
                IconButton(onClick = { navigator.push(SettingsScreen()) }) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = stringResource(R.string.settings),
                    )
                }
            },
        )
    }
}
