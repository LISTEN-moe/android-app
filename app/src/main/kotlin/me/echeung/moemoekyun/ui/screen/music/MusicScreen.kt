package me.echeung.moemoekyun.ui.screen.music

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.common.LoadingScreen
import me.echeung.moemoekyun.ui.common.SearchTextInput
import me.echeung.moemoekyun.ui.common.Toolbar
import me.echeung.moemoekyun.ui.screen.songs.SongDetails

@Composable
fun MusicScreen(onBack: () -> Unit, screenModel: MusicScreenModel = hiltViewModel()) {
    val state by screenModel.state.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            Toolbar(
                title = {
                    SearchTextInput(
                        modifier = Modifier.fillMaxWidth(),
                        query = state.searchQuery.ifEmpty { null },
                        onQueryChange = screenModel::onQueryChange,
                    )
                },
                onBack = onBack,
            )
        },
    ) { contentPadding ->
        if (state.isLoading && state.displayedSongs.isEmpty()) {
            LoadingScreen()
            return@Scaffold
        }

        val listState = rememberLazyListState()

        val shouldLoadMore by remember {
            derivedStateOf {
                val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val total = listState.layoutInfo.totalItemsCount
                total > 0 && lastVisible >= total - LOAD_MORE_THRESHOLD
            }
        }

        LaunchedEffect(shouldLoadMore) {
            if (shouldLoadMore) {
                if (state.searchQuery.isBlank()) {
                    screenModel.loadMoreLatest()
                } else {
                    screenModel.loadMoreSearch()
                }
            }
        }

        LazyColumn(
            state = listState,
            contentPadding = contentPadding,
        ) {
            if (state.searchQuery.isEmpty()) {
                item {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = stringResource(R.string.latest_songs),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    HorizontalDivider()
                }
            }

            if (!state.isLoading && state.displayedSongs.isEmpty()) {
                item {
                    Text(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        textAlign = TextAlign.Center,
                        text = stringResource(R.string.no_results, state.searchQuery),
                    )
                }
            } else {
                items(state.displayedSongs, key = { it.id }) { song ->
                    SongDetails(
                        song = song,
                        actionsEnabled = state.actionsEnabled,
                        toggleFavorite = screenModel::toggleFavorite,
                        request = screenModel::request,
                    )
                }
            }

            if (state.isLoadingMore) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier.fillParentMaxWidth().wrapContentWidth(Alignment.CenterHorizontally),
                    )
                }
            }
        }
    }
}

private const val LOAD_MORE_THRESHOLD = 10
