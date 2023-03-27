package me.echeung.moemoekyun.domain.songs.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.domain.user.UserService
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.SongsSorter
import me.echeung.moemoekyun.util.SortType
import javax.inject.Inject

class GetFavoriteSongs @Inject constructor(
    private val userService: UserService,
    private val songsSorter: SongsSorter,
    private val preferenceUtil: PreferenceUtil,
) {

    fun asFlow(): Flow<List<DomainSong>> {
        return combine(
            userService.state,
            preferenceUtil.favoritesSortType().asFlow(),
            preferenceUtil.favoritesSortDescending().asFlow(),
        ) { _, _, _ -> }
            .map { getAll() }
    }

    fun getAll(): List<DomainSong> {
        return userService.state.value.favorites.let {
            songsSorter.sort(it, preferenceUtil.favoritesSortType().get(), preferenceUtil.favoritesSortDescending().get())
        }
    }

    fun setSortType(sortType: SortType) {
        preferenceUtil.favoritesSortType().set(sortType)
    }

    fun setSortDescending(descending: Boolean) {
        preferenceUtil.favoritesSortDescending().set(descending)
    }
}
