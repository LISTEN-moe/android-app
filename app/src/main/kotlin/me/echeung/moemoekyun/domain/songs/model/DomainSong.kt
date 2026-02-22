package me.echeung.moemoekyun.domain.songs.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable

@Serializable
data class DomainSong(
    val id: Int,
    val title: String,
    val artists: String?,
    val albums: String?,
    val sources: String?,
    val duration: String,
    val durationSeconds: Long,
    val albumArtUrl: String?,
    val favorited: Boolean,
    val favoritedAtEpoch: Long?,
) {
    fun search(query: String): Boolean = title.contains(query, ignoreCase = true) ||
        artists?.contains(query, ignoreCase = true) ?: false ||
        albums?.contains(query, ignoreCase = true) ?: false ||
        sources?.contains(query, ignoreCase = true) ?: false
}

fun ImmutableList<DomainSong>.search(query: String?): ImmutableList<DomainSong> = if (query.isNullOrBlank()) {
    this
} else {
    asSequence()
        .filter { it.search(query) }
        .toImmutableList()
}
