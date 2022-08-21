package me.echeung.moemoekyun.client.model

import kotlinx.serialization.Serializable
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.client.api.Library
import java.util.Locale

@Serializable
data class Song(
    var id: Int = 0,
    var title: String? = null,
    var titleRomaji: String? = null,
    var artists: List<SongDescriptor>? = null,
    var sources: List<SongDescriptor>? = null,
    var albums: List<SongDescriptor>? = null,
    var duration: Int = 0,
    var enabled: Boolean = false,
    var favorite: Boolean = false
) {

    val titleString: String?
        get() = if (App.preferenceUtil!!.shouldPreferRomaji().get() && !titleRomaji.isNullOrBlank()) {
            titleRomaji
        } else title

    val artistsString: String?
        get() = SongDescriptor.getDisplayString(artists, App.preferenceUtil!!.shouldPreferRomaji().get())

    val albumsString: String?
        get() = SongDescriptor.getDisplayString(albums, App.preferenceUtil!!.shouldPreferRomaji().get())

    val sourcesString: String?
        get() = SongDescriptor.getDisplayString(sources, App.preferenceUtil!!.shouldPreferRomaji().get())

    val durationString: String
        get() {
            var minutes = (duration / 60).toLong()
            val seconds = (duration % 60).toLong()
            return if (minutes < 60) {
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            } else {
                val hours = minutes / 60
                minutes %= 60
                String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
            }
        }

    val albumArtUrl: String?
        get() {
            val album = albums?.firstOrNull { it.image != null }
            if (album != null) {
                return Library.CDN_ALBUM_ART_URL + album.image
            }

            return null
        }

    override fun toString(): String {
        return "$titleString - $artistsString"
    }

    fun search(query: String?): Boolean {
        if (query.isNullOrBlank()) {
            return true
        }

        return title?.contains(query, ignoreCase = true) ?: false ||
            titleRomaji?.contains(query, ignoreCase = true) ?: false ||
            artists?.any { it.contains(query) } ?: false ||
            albums?.any { it.contains(query) } ?: false ||
            sources?.any { it.contains(query) } ?: false
    }
}

fun List<Song>.search(query: String?): List<Song> {
    return asSequence()
        .filter { it.search(query) }
        .toList()
}
