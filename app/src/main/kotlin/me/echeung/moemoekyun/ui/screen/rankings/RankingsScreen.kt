package me.echeung.moemoekyun.ui.screen.rankings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.ui.common.LoadingScreen
import me.echeung.moemoekyun.ui.common.MediaListItem
import me.echeung.moemoekyun.ui.common.Toolbar
import me.echeung.moemoekyun.ui.screen.rankings.RankingsScreenModel.RankingTab
import me.echeung.moemoekyun.ui.screen.songs.SongDetails

@Composable
fun RankingsScreen(onBack: () -> Unit, screenModel: RankingsScreenModel = hiltViewModel()) {
    val state by screenModel.state.collectAsState()
    val uriHandler = LocalUriHandler.current

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            Toolbar(
                titleResId = R.string.rankings_title,
                onBack = onBack,
            )
        },
    ) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                RankingTab.entries.forEachIndexed { index, tab ->
                    SegmentedButton(
                        selected = state.selectedTab == tab,
                        onClick = { screenModel.onSelectTab(tab) },
                        shape = SegmentedButtonDefaults.itemShape(index, RankingTab.entries.size),
                    ) {
                        Text(stringResource(tab.labelRes))
                    }
                }
            }

            when {
                state.isLoading && state.currentIsEmpty -> LoadingScreen()

                state.currentIsEmpty -> EmptyMessage(R.string.rankings_empty)

                else -> RankingsList(
                    state = state,
                    onToggleFavorite = screenModel::toggleFavorite,
                    onRequest = screenModel::request,
                    onSeeFull = { uriHandler.openUri("https://listen.moe/rankings") },
                )
            }
        }
    }
}

@Composable
private fun RankingsList(
    state: RankingsScreenModel.State,
    onToggleFavorite: (Int) -> Unit,
    onRequest: (DomainSong) -> Unit,
    onSeeFull: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        when (state.selectedTab) {
            RankingTab.SONGS -> items(state.songs, key = { it.song.id }) { ranked ->
                RankedRow(rank = ranked.rank) {
                    SongDetails(
                        song = ranked.song,
                        actionsEnabled = state.actionsEnabled,
                        toggleFavorite = onToggleFavorite,
                        request = onRequest,
                    )
                }
            }

            RankingTab.ARTISTS -> items(state.artists, key = { it.rank }) { entity ->
                RankedRow(rank = entity.rank) {
                    MediaListItem(
                        title = entity.name,
                        imageUrl = entity.imageUrl,
                        subtitle = stringResource(R.string.rankings_plays, entity.count),
                        onClick = { uriHandler.openUri(entity.websiteUrl) },
                    )
                }
            }

            RankingTab.ALBUMS -> items(state.albums, key = { it.rank }) { entity ->
                RankedRow(rank = entity.rank) {
                    MediaListItem(
                        title = entity.name,
                        imageUrl = entity.imageUrl,
                        subtitle = stringResource(R.string.rankings_plays, entity.count),
                        onClick = { uriHandler.openUri(entity.websiteUrl) },
                    )
                }
            }
        }

        item {
            HorizontalDivider()

            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                TextButton(modifier = Modifier.fillMaxWidth(), onClick = onSeeFull) {
                    Text(stringResource(R.string.rankings_see_full))
                }
            }
        }
    }
}

/**
 * Wraps a ranked entry with its leading rank number, keeping numbering consistent
 * across songs, artists, and albums regardless of what each row renders.
 */
@Composable
private fun RankedRow(rank: Int, content: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = rank.toString(),
            modifier = Modifier
                .padding(start = 16.dp)
                .width(24.dp),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
        content()
    }
}

@Composable
private fun EmptyMessage(@StringRes messageRes: Int) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(messageRes),
            textAlign = TextAlign.Center,
        )
    }
}

private val RankingTab.labelRes: Int
    @StringRes get() = when (this) {
        RankingTab.SONGS -> R.string.rankings_songs
        RankingTab.ARTISTS -> R.string.rankings_artists
        RankingTab.ALBUMS -> R.string.rankings_albums
    }
