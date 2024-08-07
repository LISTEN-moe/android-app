package me.echeung.moemoekyun.ui.screen.home

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import me.echeung.moemoekyun.client.api.Station
import me.echeung.moemoekyun.domain.radio.RadioService
import me.echeung.moemoekyun.domain.radio.interactor.PlayPause
import me.echeung.moemoekyun.domain.radio.interactor.SetStation
import me.echeung.moemoekyun.domain.songs.interactor.FavoriteSong
import me.echeung.moemoekyun.domain.songs.interactor.GetFavoriteSongs
import me.echeung.moemoekyun.domain.songs.interactor.RequestSong
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.domain.songs.model.search
import me.echeung.moemoekyun.domain.user.interactor.GetAuthenticatedUser
import me.echeung.moemoekyun.domain.user.interactor.LoginLogout
import me.echeung.moemoekyun.domain.user.model.DomainUser
import me.echeung.moemoekyun.util.AlbumArtUtil
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.SortType
import me.echeung.moemoekyun.util.ext.launchIO
import javax.inject.Inject

class HomeScreenModel @Inject constructor(
    radioService: RadioService,
    private val playPause: PlayPause,
    private val setStation: SetStation,
    private val favoriteSong: FavoriteSong,
    private val requestSong: RequestSong,
    private val getAuthenticatedUser: GetAuthenticatedUser,
    private val getFavoriteSongs: GetFavoriteSongs,
    private val loginLogout: LoginLogout,
    private val albumArtUtil: AlbumArtUtil,
    private val preferenceUtil: PreferenceUtil,
) : StateScreenModel<HomeScreenModel.State>(State()) {

    val radioState = radioService.state

    init {
        screenModelScope.launchIO {
            getAuthenticatedUser.asFlow()
                .collectLatest {
                    mutableState.update { state ->
                        state.copy(
                            user = it,
                        )
                    }
                }
        }

        screenModelScope.launchIO {
            getFavoriteSongs.asFlow()
                .collectLatest {
                    mutableState.update { state ->
                        state.copy(
                            favorites = it.toImmutableList(),
                        )
                    }
                }
        }

        screenModelScope.launchIO {
            albumArtUtil.flow
                .filterNotNull()
                .collectLatest {
                    mutableState.update { state ->
                        state.copy(
                            accentColor = it.accentColor?.let { Color(it) },
                        )
                    }
                }
        }

        screenModelScope.launchIO {
            combine(
                preferenceUtil.favoritesSortType().asFlow(),
                preferenceUtil.favoritesSortDescending().asFlow(),
            ) { sortType, descending -> Pair(sortType, descending) }
                .collectLatest { (sortType, descending) ->
                    mutableState.update { state ->
                        state.copy(
                            sortType = sortType,
                            sortDescending = descending,
                        )
                    }
                }
        }
    }

    fun togglePlayState() {
        playPause.toggle()
    }

    fun toggleFavorite(songId: Int) {
        screenModelScope.launchIO {
            favoriteSong.await(songId)
        }
    }

    fun toggleLibrary(newStation: Station) {
        setStation.set(newStation)
    }

    fun logout() {
        loginLogout.logout()
    }

    fun search(query: String) {
        mutableState.update { state ->
            state.copy(
                searchQuery = query,
            )
        }
    }

    fun sortBy(sortType: SortType) {
        getFavoriteSongs.setSortType(sortType)
    }

    fun sortDescending(descending: Boolean) {
        getFavoriteSongs.setSortDescending(descending)
    }

    fun requestRandomSong() {
        screenModelScope.launchIO {
            mutableState.value.filteredFavorites?.randomOrNull()?.let {
                requestSong.await(it)
            }
        }
    }

    @Immutable
    data class State(
        val user: DomainUser? = null,
        val favorites: ImmutableList<DomainSong>? = null,
        val accentColor: Color? = null,
        val searchQuery: String? = null,
        val sortType: SortType = SortType.TITLE,
        val sortDescending: Boolean = false,
    ) {
        val filteredFavorites: ImmutableList<DomainSong>?
            get() = favorites?.search(searchQuery)
    }
}
