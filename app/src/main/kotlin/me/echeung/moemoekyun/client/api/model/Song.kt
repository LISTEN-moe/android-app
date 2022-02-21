package me.echeung.moemoekyun.client.api.model

import kotlinx.serialization.Serializable
import me.echeung.moemoekyun.client.api.Library
import me.echeung.moemoekyun.util.SongFormatter
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
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
    var favorite: Boolean = false,
) : KoinComponent {

    fun getTitleString(): String {
        val songFormatter: SongFormatter = get()
        return songFormatter.getTitle(this)
    }

    fun getArtistsString(): String {
        val songFormatter: SongFormatter = get()
        return songFormatter.getArtists(this)
    }

    fun getAlbumsString(): String {
        val songFormatter: SongFormatter = get()
        return songFormatter.getAlbums(this)
    }

    fun getSourcesString(): String {
        val songFormatter: SongFormatter = get()
        return songFormatter.getSources(this)
    }

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
        val songFormatter: SongFormatter = get()
        return "${songFormatter.getTitle(this)} - ${songFormatter.getArtists(this)}"
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
