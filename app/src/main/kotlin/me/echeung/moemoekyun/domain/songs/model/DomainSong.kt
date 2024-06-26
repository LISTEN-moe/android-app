package me.echeung.moemoekyun.domain.songs.model

import android.os.Parcelable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
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
) : Parcelable, Serializable {
    fun search(query: String): Boolean {
        return title.contains(query, ignoreCase = true) ||
            artists?.contains(query, ignoreCase = true) ?: false ||
            albums?.contains(query, ignoreCase = true) ?: false ||
            sources?.contains(query, ignoreCase = true) ?: false
    }
}

fun ImmutableList<DomainSong>.search(query: String?): ImmutableList<DomainSong> {
    return if (query.isNullOrBlank()) {
        this
    } else {
        asSequence()
            .filter { it.search(query) }
            .toImmutableList()
    }
}
