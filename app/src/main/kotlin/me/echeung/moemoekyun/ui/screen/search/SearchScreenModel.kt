package me.echeung.moemoekyun.ui.screen.search

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import me.echeung.moemoekyun.domain.songs.interactor.GetSongs
import me.echeung.moemoekyun.domain.songs.interactor.RequestSong
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.SortType
import me.echeung.moemoekyun.util.ext.launchIO
import javax.inject.Inject

class SearchScreenModel @Inject constructor(
    private val getSongs: GetSongs,
    private val requestSong: RequestSong,
    private val preferenceUtil: PreferenceUtil,
) : StateScreenModel<SearchScreenModel.State>(State()) {

    init {
        screenModelScope.launchIO {
            getSongs.asFlow()
                .collectLatest {
                    mutableState.update { state ->
                        state.copy(
                            songs = it,
                        )
                    }
                }
        }

        screenModelScope.launchIO {
            combine(
                preferenceUtil.songsSortType().asFlow(),
                preferenceUtil.songsSortDescending().asFlow(),
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

    fun requestRandomSong() {
        screenModelScope.launchIO {
            mutableState.value.filteredSongs?.randomOrNull()?.let {
                requestSong.await(it)
            }
        }
    }

    fun search(query: String) {
        mutableState.update { state ->
            state.copy(
                searchQuery = query,
            )
        }
    }

    fun sortBy(sortType: SortType) {
        getSongs.setSortType(sortType)
    }

    fun sortDescending(descending: Boolean) {
        getSongs.setSortDescending(descending)
    }

    @Immutable
    data class State(
        val songs: List<DomainSong>? = null,
        val searchQuery: String? = null,
        val sortType: SortType = SortType.TITLE,
        val sortDescending: Boolean = false,
    ) {
        val filteredSongs: List<DomainSong>?
            get() = songs
                ?.filter { searchQuery.isNullOrBlank() || it.search(searchQuery) }
    }
}
