package me.echeung.moemoekyun.ui.screen.rankings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import me.echeung.moemoekyun.client.api.ChartEntityEntry
import me.echeung.moemoekyun.client.api.ChartsApiClient
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

@HiltViewModel
class RankingsScreenModel @Inject constructor(
    private val chartsApiClient: ChartsApiClient,
    private val songConverter: SongConverter,
    private val favoriteSong: FavoriteSong,
    private val requestSong: RequestSong,
    private val getAuthenticatedUser: GetAuthenticatedUser,
    private val preferenceUtil: PreferenceUtil,
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        loadTab(RankingTab.SONGS)
    }

    fun onSelectTab(tab: RankingTab) {
        _state.update { it.copy(selectedTab = tab) }
        loadTab(tab)
    }

    fun toggleFavorite(songId: Int) {
        viewModelScope.launchIO {
            val favorited = favoriteSong.await(songId)
            _state.update { s ->
                s.copy(
                    songs = s.songs.map {
                        if (it.song.id == songId) it.copy(song = it.song.copy(favorited = favorited)) else it
                    }.toImmutableList(),
                )
            }
        }
    }

    fun request(song: DomainSong) {
        viewModelScope.launchIO { requestSong.await(song) }
    }

    private fun loadTab(tab: RankingTab) {
        // Skip refetching a tab whose data is already loaded.
        val current = _state.value
        val alreadyLoaded = when (tab) {
            RankingTab.SONGS -> current.songs.isNotEmpty()
            RankingTab.ARTISTS -> current.artists.isNotEmpty()
            RankingTab.ALBUMS -> current.albums.isNotEmpty()
        }
        if (alreadyLoaded) return

        viewModelScope.launchIO {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val actionsEnabled = getAuthenticatedUser.get() != null
                when (tab) {
                    RankingTab.SONGS -> {
                        val response = chartsApiClient.songs(station())
                        val songs = response.entries.map {
                            RankedSong(rank = it.rank, song = songConverter.toDomainSong(it.entity.transform()))
                        }.toImmutableList()
                        _state.update { it.copy(songs = songs, actionsEnabled = actionsEnabled, isLoading = false) }
                    }

                    RankingTab.ARTISTS -> {
                        val response = chartsApiClient.artists(station())
                        val artists = response.entries.map { it.toRankingEntity(EntityKind.ARTISTS) }.toImmutableList()
                        _state.update { it.copy(artists = artists, actionsEnabled = actionsEnabled, isLoading = false) }
                    }

                    RankingTab.ALBUMS -> {
                        val response = chartsApiClient.albums(station())
                        val albums = response.entries.map { it.toRankingEntity(EntityKind.ALBUMS) }.toImmutableList()
                        _state.update { it.copy(albums = albums, actionsEnabled = actionsEnabled, isLoading = false) }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun station(): String = if (preferenceUtil.station().get() == Station.KPOP) "kpop" else "jpop"

    private fun ChartEntityEntry.toRankingEntity(kind: EntityKind): RankingEntity {
        val preferRomaji = preferenceUtil.shouldPreferRomaji().get()
        val name = (if (preferRomaji) entity.nameRomaji ?: entity.name else entity.name).orEmpty()
        val imageUrl = entity.image?.let { "https://cdn.listen.moe/${kind.cdnPath}/$it" }
        return RankingEntity(
            rank = rank,
            name = name,
            imageUrl = imageUrl,
            count = count,
            websiteUrl = "https://listen.moe/${kind.webPath}/${entity.id}",
        )
    }

    private enum class EntityKind(val cdnPath: String, val webPath: String) {
        ARTISTS("artists", "artists"),
        ALBUMS("covers", "albums"),
    }

    enum class RankingTab {
        SONGS,
        ARTISTS,
        ALBUMS,
    }

    @Immutable
    data class RankedSong(val rank: Int, val song: DomainSong)

    @Immutable
    data class RankingEntity(
        val rank: Int,
        val name: String,
        val imageUrl: String?,
        val count: Int,
        val websiteUrl: String,
    )

    @Immutable
    data class State(
        val selectedTab: RankingTab = RankingTab.SONGS,
        val songs: ImmutableList<RankedSong> = persistentListOf(),
        val artists: ImmutableList<RankingEntity> = persistentListOf(),
        val albums: ImmutableList<RankingEntity> = persistentListOf(),
        val isLoading: Boolean = false,
        val actionsEnabled: Boolean = false,
        val error: String? = null,
    ) {
        val currentIsEmpty: Boolean
            get() = when (selectedTab) {
                RankingTab.SONGS -> songs.isEmpty()
                RankingTab.ARTISTS -> artists.isEmpty()
                RankingTab.ALBUMS -> albums.isEmpty()
            }
    }
}
