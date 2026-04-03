package me.echeung.moemoekyun.ui.screen.music

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.cache.http.HttpFetchPolicy
import com.apollographql.apollo.cache.http.httpFetchPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import me.echeung.moemoekyun.LatestSongsQuery
import me.echeung.moemoekyun.client.api.SearchApiClient
import me.echeung.moemoekyun.client.api.SearchResult
import me.echeung.moemoekyun.client.api.Station
import me.echeung.moemoekyun.client.api.data.transform
import me.echeung.moemoekyun.domain.songs.interactor.FavoriteSong
import me.echeung.moemoekyun.domain.songs.interactor.RequestSong
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.domain.songs.model.SongConverter
import me.echeung.moemoekyun.domain.user.interactor.GetAuthenticatedUser
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.ext.launchIO
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class MusicScreenModel @Inject constructor(
    private val apolloClient: ApolloClient,
    private val searchApiClient: SearchApiClient,
    private val songConverter: SongConverter,
    private val favoriteSong: FavoriteSong,
    private val requestSong: RequestSong,
    private val getAuthenticatedUser: GetAuthenticatedUser,
    private val preferenceUtil: PreferenceUtil,
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        fetchLatest()

        viewModelScope.launchIO {
            _state
                .map { it.searchQuery }
                .debounce(300L)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        if (_state.value.latestSongs.isEmpty()) {
                            fetchLatest()
                        } else {
                            _state.update { it.copy(searchSongs = emptyList(), nextCursor = null) }
                        }
                    } else {
                        runSearch(query)
                    }
                }
        }
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun loadMoreLatest() {
        val state = _state.value
        if (state.searchQuery.isNotBlank() || state.isLoadingMore || !state.hasMoreLatest) return
        val nextOffset = state.latestSongs.size
        loadMore {
            val response = apolloClient.query(
                LatestSongsQuery(PAGE_SIZE, nextOffset, kpopOptional()),
            ).httpFetchPolicy(HttpFetchPolicy.NetworkOnly).execute()
            val newSongs = response.data?.songs?.songs?.map { it.toDomainSong() }.orEmpty()
            val totalCount = response.data?.songs?.count ?: 0
            _state.update { s ->
                s.copy(
                    latestSongs = s.latestSongs + newSongs,
                    hasMoreLatest = s.latestSongs.size + newSongs.size < totalCount,
                )
            }
        }
    }

    fun loadMoreSearch() {
        val state = _state.value
        val cursor = state.nextCursor ?: return
        if (state.isLoadingMore || state.searchQuery.isBlank()) return
        loadMore {
            val response = searchApiClient.search(state.searchQuery, cursor)
            val newSongs = response.results.map { it.toDomainSong() }
            _state.update { s ->
                s.copy(
                    searchSongs = s.searchSongs + newSongs,
                    nextCursor = response.nextCursor,
                )
            }
        }
    }

    fun toggleFavorite(songId: Int) {
        viewModelScope.launchIO {
            val favorited = favoriteSong.await(songId)
            _state.update { s ->
                s.copy(
                    latestSongs = s.latestSongs.map { if (it.id == songId) it.copy(favorited = favorited) else it },
                    searchSongs = s.searchSongs.map { if (it.id == songId) it.copy(favorited = favorited) else it },
                )
            }
        }
    }

    fun request(song: DomainSong) {
        viewModelScope.launchIO { requestSong.await(song) }
    }

    private fun fetchLatest() {
        viewModelScope.launchIO {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apolloClient.query(
                    LatestSongsQuery(PAGE_SIZE, 0, kpopOptional()),
                ).httpFetchPolicy(HttpFetchPolicy.NetworkOnly).execute()
                val songs = response.data?.songs?.songs?.map { it.toDomainSong() }.orEmpty()
                val totalCount = response.data?.songs?.count ?: 0
                _state.update {
                    it.copy(
                        latestSongs = songs,
                        hasMoreLatest = songs.size < totalCount,
                        isLoading = false,
                        actionsEnabled = getAuthenticatedUser.get() != null,
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private suspend fun runSearch(query: String) {
        _state.update { it.copy(isLoading = true, searchSongs = emptyList(), nextCursor = null) }
        try {
            val response = searchApiClient.search(query)
            _state.update {
                it.copy(
                    searchSongs = response.results.map { r -> r.toDomainSong() },
                    nextCursor = response.nextCursor,
                    isLoading = false,
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    private fun loadMore(block: suspend () -> Unit) {
        viewModelScope.launchIO {
            _state.update { it.copy(isLoadingMore = true) }
            try {
                block()
            } catch (_: Exception) {
            } finally {
                _state.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    private fun LatestSongsQuery.Song.toDomainSong() = songConverter.toDomainSong(transform())
    private fun SearchResult.toDomainSong() = songConverter.toDomainSong(transform())

    private fun kpopOptional(): Optional<Boolean?> {
        val isKpop = preferenceUtil.station().get() == Station.KPOP
        return Optional.presentIfNotNull(isKpop.takeIf { it })
    }

    @Immutable
    data class State(
        val latestSongs: List<DomainSong> = emptyList(),
        val searchSongs: List<DomainSong> = emptyList(),
        val searchQuery: String = "",
        val nextCursor: String? = null,
        val hasMoreLatest: Boolean = false,
        val isLoading: Boolean = false,
        val isLoadingMore: Boolean = false,
        val actionsEnabled: Boolean = false,
        val error: String? = null,
    ) {
        val displayedSongs: List<DomainSong> get() = if (searchQuery.isBlank()) latestSongs else searchSongs
    }

    companion object {
        private val PAGE_SIZE = SearchApiClient.PAGE_SIZE
    }
}
