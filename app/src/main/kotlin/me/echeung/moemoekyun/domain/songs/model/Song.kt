package me.echeung.moemoekyun.domain.songs.model

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
) {
    fun search(query: String?): Boolean {
        if (query.isNullOrBlank()) {
            return true
        }

        return title.contains(query, ignoreCase = true) ||
            artists?.contains(query) ?: false ||
            albums?.contains(query) ?: false ||
            sources?.contains(query) ?: false
    }
}

fun List<DomainSong>.search(query: String?): List<DomainSong> {
    return asSequence()
        .filter { it.search(query) }
        .toList()
}
