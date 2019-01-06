package me.echeung.moemoekyun.client.model

import android.text.TextUtils
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.client.api.v4.library.Library
import java.util.Locale

class Song {
    var id: Int = 0
    var title: String? = null
    var titleRomaji: String? = null
    var titleSearchRomaji: String? = null
    var albums: List<SongDescriptor>? = null
    var artists: List<SongDescriptor>? = null
    var sources: List<SongDescriptor>? = null
    var groups: List<String>? = null
    var tags: List<String>? = null
    var notes: String? = null
    var duration: Int = 0
    var enabled: Boolean = false
    var uploader: User? = null
    var favorite: Boolean = false

    val titleString: String?
        get() = if (App.preferenceUtil!!.shouldPreferRomaji() && !TextUtils.isEmpty(titleRomaji)) {
            titleRomaji
        } else title

    val albumsString: String
        get() = SongDescriptor.getSongDescriptorsString(albums)

    val artistsString: String
        get() = SongDescriptor.getSongDescriptorsString(artists)

    val sourcesString: String
        get() = SongDescriptor.getSongDescriptorsString(sources)

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
            if (!albums!!.isEmpty()) {
                for (album in albums!!) {
                    if (album.image != null) {
                        return Library.CDN_ALBUM_ART_URL + album.image
                    }
                }
            }

            return null
        }

    override fun toString(): String {
        return "$titleString - $artistsString"
    }

    fun search(query: String): Boolean {
        val query = query.toLowerCase().trim()

        if (title != null && title!!.toLowerCase().contains(query)) {
            return true
        }

        if (titleRomaji != null && titleRomaji!!.toLowerCase().contains(query)) {
            return true
        }

        if (titleSearchRomaji != null && titleSearchRomaji!!.toLowerCase().contains(query)) {
            return true
        }

        if (albums != null) {
            for (album in albums!!) {
                if (album.name != null && album.name!!.toLowerCase().contains(query)) {
                    return true
                }
                if (album.nameRomaji != null && album.nameRomaji!!.toLowerCase().contains(query)) {
                    return true
                }
            }
        }

        if (artists != null) {
            for (artist in artists!!) {
                if (artist.name != null && artist.name!!.toLowerCase().contains(query)) {
                    return true
                }
                if (artist.nameRomaji != null && artist.nameRomaji!!.toLowerCase().contains(query)) {
                    return true
                }
            }
        }

        if (groups != null) {
            for (group in groups!!) {
                if (group.toLowerCase().contains(query)) {
                    return true
                }
            }
        }

        if (tags != null) {
            for (tag in tags!!) {
                if (tag.toLowerCase().contains(query)) {
                    return true
                }
            }
        }

        return false
    }
}
