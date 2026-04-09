package me.echeung.moemoekyun.ui.screen.songs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.collections.immutable.ImmutableList
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.domain.songs.model.DomainSong

@Composable
fun SongsScreen(songs: ImmutableList<DomainSong>, moreUrl: String? = null) {
    val screenModel = hiltViewModel<SongsScreenModel, SongsScreenModel.Factory> { factory ->
        factory.create(songs)
    }
    val state by screenModel.state.collectAsState()
    val uriHandler = LocalUriHandler.current

    Surface {
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.systemBars),
            ) {
                itemsIndexed(state.songs) { index, item ->
                    SongDetails(
                        song = item,
                        actionsEnabled = state.actionsEnabled,
                        toggleFavorite = screenModel::toggleFavorite,
                        request = screenModel::request,
                    )

                    if (index < state.songs.lastIndex) {
                        HorizontalDivider()
                    }
                }

                if (moreUrl != null) {
                    item {
                        HorizontalDivider()

                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            TextButton(modifier = Modifier.fillMaxWidth(), onClick = { uriHandler.openUri(moreUrl) }) {
                                Text(stringResource(R.string.see_more))
                            }
                        }
                    }
                }
            }
        }
    }
}
