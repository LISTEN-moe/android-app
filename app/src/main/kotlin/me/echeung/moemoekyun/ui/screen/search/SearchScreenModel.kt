package me.echeung.moemoekyun.ui.screen.search

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import me.echeung.moemoekyun.domain.songs.interactor.GetSongs
import me.echeung.moemoekyun.domain.songs.interactor.RequestSong
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.domain.songs.model.search
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.SortType
import me.echeung.moemoekyun.util.ext.launchIO
import javax.inject.Inject

@HiltViewModel
class SearchScreenModel @Inject constructor(
    private val getSongs: GetSongs,
    private val requestSong: RequestSong,
    private val preferenceUtil: PreferenceUtil,
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launchIO {
            getSongs.asFlow()
                .collectLatest {
                    _state.update { state ->
                        state.copy(
                            songs = it.toImmutableList(),
                        )
                    }
                }
        }

        viewModelScope.launchIO {
            combine(
                preferenceUtil.songsSortType().asFlow(),
                preferenceUtil.songsSortDescending().asFlow(),
            ) { sortType, descending -> Pair(sortType, descending) }
                .collectLatest { (sortType, descending) ->
                    _state.update { state ->
                        state.copy(
                            sortType = sortType,
                            sortDescending = descending,
                        )
                    }
                }
        }
    }

    fun requestRandomSong() {
        viewModelScope.launchIO {
            _state.value.filteredSongs?.randomOrNull()?.let {
                requestSong.await(it)
            }
        }
    }

    fun search(query: String) {
        _state.update { state ->
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
        val songs: ImmutableList<DomainSong>? = null,
        val searchQuery: String? = null,
        val sortType: SortType = SortType.TITLE,
        val sortDescending: Boolean = false,
    ) {
        val filteredSongs: ImmutableList<DomainSong>?
            get() = songs?.search(searchQuery)
    }
}
