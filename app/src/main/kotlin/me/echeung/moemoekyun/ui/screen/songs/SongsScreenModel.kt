package me.echeung.moemoekyun.ui.screen.songs

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import cafe.adriel.voyager.hilt.ScreenModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.update
import me.echeung.moemoekyun.domain.songs.interactor.FavoriteSong
import me.echeung.moemoekyun.domain.songs.interactor.GetSong
import me.echeung.moemoekyun.domain.songs.interactor.RequestSong
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.domain.user.interactor.GetAuthenticatedUser
import me.echeung.moemoekyun.util.ext.launchIO

class SongsScreenModel @AssistedInject constructor(
    @Assisted val songs: List<DomainSong>,
    private val getSong: GetSong,
    private val favoriteSong: FavoriteSong,
    private val requestSong: RequestSong,
    private val getAuthenticatedUser: GetAuthenticatedUser,
) : StateScreenModel<SongsScreenModel.State>(State(songs)) {

    init {
        coroutineScope.launchIO {
            val detailedSongs = songs.map { getSong.await(it.id) }

            mutableState.update {
                it.copy(
                    songs = detailedSongs,
                    actionsEnabled = getAuthenticatedUser.get() != null,
                )
            }
        }
    }

    fun toggleFavorite(songId: Int) {
        coroutineScope.launchIO {
            val favorited = favoriteSong.await(songId)
        }
    }

    fun request(song: DomainSong) {
        coroutineScope.launchIO {
            requestSong.await(song)
        }
    }

    @AssistedFactory
    interface Factory : ScreenModelFactory {
        fun create(songs: List<DomainSong>): SongsScreenModel
    }

    data class State(
        val songs: List<DomainSong>,
        val actionsEnabled: Boolean = false,
    )
}
