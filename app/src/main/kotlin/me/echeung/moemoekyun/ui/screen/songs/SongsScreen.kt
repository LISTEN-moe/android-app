package me.echeung.moemoekyun.ui.screen.songs

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getScreenModel
import me.echeung.moemoekyun.domain.songs.model.DomainSong

data class SongsScreen(private val songs: List<DomainSong>) : Screen {

    @Composable
    override fun Content() {
        val screenModel = getScreenModel<SongsScreenModel, SongsScreenModel.Factory> { factory ->
            factory.create(songs)
        }
        val state by screenModel.state.collectAsState()

        Surface {
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
            }
        }
    }
}
