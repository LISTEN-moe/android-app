package me.echeung.moemoekyun.ui.screen.songs

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import me.echeung.moemoekyun.domain.songs.interactor.FavoriteSong
import me.echeung.moemoekyun.domain.songs.interactor.GetSong
import me.echeung.moemoekyun.domain.songs.interactor.RequestSong
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.domain.user.interactor.GetAuthenticatedUser
import me.echeung.moemoekyun.util.ext.launchIO

@HiltViewModel(assistedFactory = SongsScreenModel.Factory::class)
class SongsScreenModel @AssistedInject constructor(
    @Assisted val songs: List<DomainSong>,
    private val getSong: GetSong,
    private val favoriteSong: FavoriteSong,
    private val requestSong: RequestSong,
    private val getAuthenticatedUser: GetAuthenticatedUser,
) : ViewModel() {

    private val _state = MutableStateFlow(State(songs))
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launchIO {
            val detailedSongs = songs.map { getSong.await(it.id) }

            _state.update { state ->
                state.copy(
                    songs = detailedSongs,
                    actionsEnabled = getAuthenticatedUser.get() != null,
                )
            }
        }
    }

    fun toggleFavorite(songId: Int) {
        viewModelScope.launchIO {
            val favorited = favoriteSong.await(songId)

            _state.update { state ->
                state.copy(
                    songs = state.songs.map {
                        if (it.id == songId) {
                            it.copy(favorited = favorited)
                        } else {
                            it
                        }
                    },
                    actionsEnabled = getAuthenticatedUser.get() != null,
                )
            }
        }
    }

    fun request(song: DomainSong) {
        viewModelScope.launchIO {
            requestSong.await(song)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(songs: List<DomainSong>): SongsScreenModel
    }

    @Immutable
    data class State(val songs: List<DomainSong>, val actionsEnabled: Boolean = false)
}
