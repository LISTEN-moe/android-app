package me.echeung.moemoekyun.ui.screen.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.api.Station
import me.echeung.moemoekyun.ui.common.BackgroundBox
import me.echeung.moemoekyun.ui.common.DropdownMenu
import me.echeung.moemoekyun.ui.common.Toolbar
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

        PlayerScaffold(
            radioState = radioState,
            accentColor = state.accentColor,
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
                topBar = {
                    Toolbar(
                        actions = {
                            HomeAppBarActions(
                                screenModel = screenModel,
                                onClickLogOut = screenModel::logout.takeIf { state.user != null },
                            )
                        },
                    )
                },
            ) { contentPadding ->
                BackgroundBox(
                    modifier = Modifier
                        .padding(contentPadding)
                        .fillMaxSize(),
                ) {
                    Box(modifier = Modifier.padding(bottom = PlayerPeekHeight)) {
                        if (state.user == null) {
                            UnauthedHomeContent()
                        } else {
                            AuthedHomeContent(
                                user = state.user!!,
                                favorites = state.filteredFavorites,
                                query = state.searchQuery,
                                onQueryChange = screenModel::search,
                                sortType = state.sortType,
                                onSortBy = screenModel::sortBy,
                                sortDescending = state.sortDescending,
                                onSortDescending = screenModel::sortDescending,
                                requestRandomSong = screenModel::requestRandomSong,
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun HomeAppBarActions(
        screenModel: HomeScreenModel,
        onClickLogOut: (() -> Unit)?,
    ) {
        val navigator = LocalNavigator.currentOrThrow

        val radioState by screenModel.radioState.collectAsState()

        var showLibraryMenu by remember { mutableStateOf(false) }
        var showOverflowMenu by remember { mutableStateOf(false) }
        var showLogoutConfirmation by remember { mutableStateOf(false) }

        IconButton(onClick = { navigator.push(SearchScreen()) }) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = stringResource(R.string.search),
            )
        }
        IconButton(onClick = { showLibraryMenu = !showLibraryMenu }) {
            Icon(
                imageVector = Icons.Outlined.LibraryMusic,
                contentDescription = stringResource(R.string.action_library),
            )
        }
        DropdownMenu(
            expanded = showLibraryMenu,
            onDismissRequest = { showLibraryMenu = false },
        ) {
            Station.values().map {
                DropdownMenuItem(
                    onClick = {
                        screenModel.toggleLibrary(it)
                        showLibraryMenu = false
                    },
                    text = { Text(stringResource(it.labelRes)) },
                    trailingIcon = {
                        Icon(
                            if (radioState.station == it) {
                                Icons.Outlined.RadioButtonChecked
                            } else {
                                Icons.Outlined.RadioButtonUnchecked
                            },
                            contentDescription = null,
                        )
                    },
                )
            }
        }

        IconButton(onClick = { showOverflowMenu = !showOverflowMenu }) {
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.action_more))
        }
        DropdownMenu(
            expanded = showOverflowMenu,
            onDismissRequest = { showOverflowMenu = false },
        ) {
            DropdownMenuItem(
                onClick = { navigator.push(AboutScreen()) },
                text = { Text(stringResource(R.string.about)) },
            )
            DropdownMenuItem(
                onClick = { navigator.push(SettingsScreen()) },
                text = { Text(stringResource(R.string.settings)) },
            )
            onClickLogOut?.let {
                DropdownMenuItem(
                    onClick = {
                        showLogoutConfirmation = true
                        showOverflowMenu = false
                    },
                    text = { Text(stringResource(R.string.logout)) },
                )
            }
        }

        if (showLogoutConfirmation && onClickLogOut != null) {
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
}
