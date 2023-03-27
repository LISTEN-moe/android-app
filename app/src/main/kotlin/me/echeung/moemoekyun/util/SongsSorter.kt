package me.echeung.moemoekyun.util

import me.echeung.moemoekyun.domain.songs.model.DomainSong
import javax.inject.Inject

class SongsSorter @Inject constructor() {

    fun sort(songs: Collection<DomainSong>, sortType: SortType, descending: Boolean): List<DomainSong> {
        return songs
            .distinctBy { it.id }
            .sortedWith(getComparator(sortType, descending))
    }

    private fun getComparator(sortType: SortType, descending: Boolean): Comparator<DomainSong> {
        return when (sortType) {
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
        }
    }
}

enum class SortType {
    TITLE,
    ARTIST,
}
