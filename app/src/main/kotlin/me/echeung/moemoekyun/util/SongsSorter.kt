package me.echeung.moemoekyun.util

import androidx.annotation.StringRes
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import javax.inject.Inject

class SongsSorter @Inject constructor() {

    fun sort(songs: Collection<DomainSong>, sortType: SortType, descending: Boolean): List<DomainSong> = songs
        .distinctBy { it.id }
        .sortedWith(getComparator(sortType, descending))

    private fun getComparator(sortType: SortType, descending: Boolean): Comparator<DomainSong> = when (sortType) {
        SortType.TITLE ->
            if (descending) {
                compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.title }
            } else {
                compareBy(String.CASE_INSENSITIVE_ORDER) { it.title }
            }

        SortType.ARTIST ->
            if (descending) {
                compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.artists.orEmpty() }
            } else {
                compareBy(String.CASE_INSENSITIVE_ORDER) { it.artists.orEmpty() }
            }

        SortType.FAVORITED_AT ->
            if (descending) {
                compareByDescending { it.favoritedAtEpoch }
            } else {
                compareBy { it.favoritedAtEpoch }
            }
    }
}

enum class SortType(@StringRes val labelRes: Int) {
    TITLE(R.string.sort_title),
    ARTIST(R.string.sort_artist),
    FAVORITED_AT(R.string.sort_favorited_at),
}
