package me.echeung.moemoekyun.domain.songs.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import me.echeung.moemoekyun.domain.songs.SongsService
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.SongsSorter
import me.echeung.moemoekyun.util.SortType
import javax.inject.Inject

class GetSongs @Inject constructor(
    private val songsService: SongsService,
    private val songsSorter: SongsSorter,
    private val getFavoriteSongs: GetFavoriteSongs,
    private val preferenceUtil: PreferenceUtil,
) {

    fun asFlow(): Flow<List<DomainSong>> {
        return combine(
            songsService.songs,
            getFavoriteSongs.asFlow(),
            preferenceUtil.songsSortType().asFlow(),
            preferenceUtil.songsSortDescending().asFlow(),
        ) { songs, favorites, _, _ -> Pair(songs, favorites.map { it.id }) }
            .map { (songs, favorites) ->
                songs.values.map { it.copy(favorited = it.id in favorites) }
            }
            .map {
                songsSorter.sort(it, preferenceUtil.songsSortType().get(), preferenceUtil.songsSortDescending().get())
            }
    }

    fun setSortType(sortType: SortType) {
        preferenceUtil.songsSortType().set(sortType)
    }

    fun setSortDescending(descending: Boolean) {
        preferenceUtil.songsSortDescending().set(descending)
    }
}
