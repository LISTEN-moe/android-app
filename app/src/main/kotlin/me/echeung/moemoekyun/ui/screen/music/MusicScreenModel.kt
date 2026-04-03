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
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.client.model.SongDescriptor
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
                        _state.update { it.copy(mode = Mode.Latest) }
                        if (_state.value.latestSongs.isEmpty()) fetchLatest()
                    } else {
                        runSearch(query, cursor = null)
                    }
                }
        }
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun loadMoreLatest() {
        val state = _state.value
        if (state.mode !is Mode.Latest || state.isLoadingMore || !state.hasMoreLatest) return
        val nextOffset = state.latestSongs.size
        viewModelScope.launchIO {
            _state.update { it.copy(isLoadingMore = true) }
            try {
                val response = apolloClient.query(
                    LatestSongsQuery(PAGE_SIZE, nextOffset, kpopOptional()),
                ).httpFetchPolicy(HttpFetchPolicy.NetworkOnly).execute()
                val newSongs = response.data?.songs?.songs?.map { it.toDomainSong() }.orEmpty()
                val totalCount = response.data?.songs?.count ?: 0
                _state.update { s ->
                    s.copy(
                        latestSongs = s.latestSongs + newSongs,
                        hasMoreLatest = s.latestSongs.size + newSongs.size < totalCount,
                        isLoadingMore = false,
                    )
                }
            } catch (_: Exception) {
                _state.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    fun loadMoreSearch() {
        val state = _state.value
        val cursor = state.nextCursor ?: return
        if (state.isLoadingMore || state.mode !is Mode.Searching) return
        viewModelScope.launchIO {
            _state.update { it.copy(isLoadingMore = true) }
            try {
                val response = searchApiClient.search(state.searchQuery, cursor)
                val newSongs = response.results.map { it.toDomainSong() }
                _state.update { s ->
                    s.copy(
                        searchSongs = s.searchSongs + newSongs,
                        nextCursor = response.nextCursor,
                        isLoadingMore = false,
                    )
                }
            } catch (_: Exception) {
                _state.update { it.copy(isLoadingMore = false) }
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
                val actionsEnabled = getAuthenticatedUser.get() != null
                _state.update {
                    it.copy(
                        mode = Mode.Latest,
                        latestSongs = songs,
                        hasMoreLatest = songs.size < totalCount,
                        isLoading = false,
                        actionsEnabled = actionsEnabled,
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private suspend fun runSearch(query: String, cursor: String?) {
        val resetSongs = cursor == null
        _state.update {
            it.copy(
                mode = Mode.Searching,
                isLoading = resetSongs,
                searchSongs = if (resetSongs) emptyList() else it.searchSongs,
            )
        }
        try {
            val response = searchApiClient.search(query, cursor)
            val songs = response.results.map { it.toDomainSong() }
            _state.update { s ->
                s.copy(
                    searchSongs = songs,
                    nextCursor = response.nextCursor,
                    isLoading = false,
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    private fun LatestSongsQuery.Song.toDomainSong(): DomainSong = songConverter.toDomainSong(transform())

    private fun SearchResult.toDomainSong(): DomainSong {
        val song = Song(
            id = id,
            title = title,
            titleRomaji = titleRomaji,
            artists = artists.map { SongDescriptor(it.name, it.nameRomaji, it.image) },
            albums = albums.map { SongDescriptor(it.name, it.nameRomaji, it.image) },
            sources = sources.map { SongDescriptor(it.name, it.nameRomaji, it.image) },
            duration = duration,
        )
        return songConverter.toDomainSong(song)
    }

    private fun kpopOptional(): Optional<Boolean?> {
        val isKpop = preferenceUtil.station().get() == Station.KPOP
        return Optional.presentIfNotNull(isKpop.takeIf { it })
    }

    sealed interface Mode {
        data object Latest : Mode
        data object Searching : Mode
    }

    @Immutable
    data class State(
        val mode: Mode = Mode.Latest,
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
        val displayedSongs: List<DomainSong> get() = when (mode) {
            Mode.Latest -> latestSongs
            Mode.Searching -> searchSongs
        }
    }

    companion object {
        private val PAGE_SIZE = SearchApiClient.PAGE_SIZE
    }
}
