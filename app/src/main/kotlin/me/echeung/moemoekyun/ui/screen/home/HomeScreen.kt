package me.echeung.moemoekyun.ui.screen.home

import android.content.ComponentName
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.service.PlaybackService
import me.echeung.moemoekyun.ui.common.BackgroundBox
import me.echeung.moemoekyun.ui.common.toolbarColors

@Composable
fun HomeScreen(
    onNavigateSearch: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateAbout: () -> Unit,
    onNavigateLogin: () -> Unit,
    onNavigateRegister: () -> Unit,
    onShowHistory: (List<DomainSong>) -> Unit,
) {
    val screenModel = hiltViewModel<HomeScreenModel>()
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

    PlayerScaffold(
        radioState = radioState,
        mediaController = player,
        accentColor = state.accentColor,
        onClickStation = screenModel::toggleLibrary,
        onClickHistory = {
            val historySongs = listOfNotNull(radioState.currentSong) + radioState.pastSongs
            if (historySongs.isNotEmpty()) {
                onShowHistory(historySongs)
            }
        },
        toggleFavorite = screenModel::toggleFavorite.takeIf { isAuthenticated },
        content = {
            Scaffold(
                topBar = {
                    HomeToolbar(
                        isAuthenticated = isAuthenticated,
                        onNavigateSearch = onNavigateSearch,
                        onNavigateAbout = onNavigateAbout,
                        onNavigateSettings = onNavigateSettings,
                    )
                },
            ) { contentPadding ->
                BackgroundBox(
                    modifier = Modifier.fillMaxSize(),
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
                            onShowSongs = onShowHistory,
                            contentPadding = contentPadding,
                        )
                    } else {
                        UnauthedHomeContent(
                            onNavigateLogin = onNavigateLogin,
                            onNavigateRegister = onNavigateRegister,
                            contentPadding = contentPadding,
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun HomeToolbar(
    isAuthenticated: Boolean,
    onNavigateSearch: () -> Unit,
    onNavigateAbout: () -> Unit,
    onNavigateSettings: () -> Unit,
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            if (isAuthenticated) {
                IconButton(onClick = onNavigateSearch) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = stringResource(R.string.search),
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onNavigateAbout) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = stringResource(R.string.about),
                )
            }
            IconButton(onClick = onNavigateSettings) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.settings),
                )
            }
        },
        colors = toolbarColors(),
    )
}
