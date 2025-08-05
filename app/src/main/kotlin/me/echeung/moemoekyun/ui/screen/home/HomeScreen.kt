package me.echeung.moemoekyun.ui.screen.home

import android.content.ComponentName
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
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.common.util.concurrent.MoreExecutors
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.service.PlaybackService
import me.echeung.moemoekyun.ui.common.BackgroundBox
import me.echeung.moemoekyun.ui.common.ToolbarColors
import me.echeung.moemoekyun.ui.screen.about.AboutScreen
import me.echeung.moemoekyun.ui.screen.search.SearchScreen
import me.echeung.moemoekyun.ui.screen.settings.SettingsScreen
import me.echeung.moemoekyun.ui.screen.songs.SongsScreen

object HomeScreen : Screen {

    @Composable
    override fun Content() {
        val bottomSheetNavigator = LocalBottomSheetNavigator.current

        val screenModel = getScreenModel<HomeScreenModel>()
        val state by screenModel.state.collectAsState()

        val radioState by screenModel.radioState.collectAsState()
        val isAuthenticated = state.user != null

        val context = LocalContext.current

        val player by produceState<MediaController?>(null) {
            val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))

            // Build the MediaController asynchronously
            val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

            // Set up a listener to handle the controller when it's ready
            controllerFuture.addListener(
                { value = controllerFuture.get() },
                MoreExecutors.directExecutor(),
            )
        }

        if (player == null) {
            return
        }

        PlayerScaffold(
            radioState = radioState,
            mediaController = player!!,
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
            toggleFavorite = screenModel::toggleFavorite.takeIf { isAuthenticated },
            content = {
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
            },
        )
    }

    @Composable
    private fun Toolbar(isAuthenticated: Boolean) {
        val navigator = LocalNavigator.currentOrThrow

        TopAppBar(
            title = {},
            navigationIcon = {
                if (isAuthenticated) {
                    IconButton(onClick = { navigator.push(SearchScreen) }) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = stringResource(R.string.search),
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = { navigator.push(AboutScreen) }) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = stringResource(R.string.about),
                    )
                }
                IconButton(onClick = { navigator.push(SettingsScreen) }) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = stringResource(R.string.settings),
                    )
                }
            },
            colors = ToolbarColors(),
        )
    }
}
