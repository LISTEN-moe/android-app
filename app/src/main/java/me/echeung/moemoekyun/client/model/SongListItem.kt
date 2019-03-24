package me.echeung.moemoekyun.client.model

import java.util.ArrayList

class SongListItem {
    var albums: List<String>? = null
    var albumsId: List<Int>? = null
    var albumsCover: List<String>? = null
    var albumsRomaji: List<String>? = null
    var albumsSearchRomaji: List<String>? = null
    var artists: List<String>? = null
    var artistsId: List<Int>? = null
    var artistsRomaji: List<String>? = null
    var artistsSearchRomaji: List<String>? = null
    var duration: Int = 0
    var enabled: Boolean = false
    var favorite: Boolean = false
    var groups: List<String>? = null
    var id: Int = 0
    var sources: List<String>? = null
    var sourcesRomaji: List<String>? = null
    var tags: List<String>? = null
    var title: String? = null
    var titleRomaji: String? = null
    var titleSearchRomaji: String? = null

    fun search(query: String?): Boolean {
        if (query.isNullOrEmpty()) {
            return true
        }

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
                if (album.toLowerCase().contains(query)) {
                    return true
                }
            }
        }

        if (albumsRomaji != null) {
            for (album in albumsRomaji!!) {
                if (album.toLowerCase().contains(query)) {
                    return true
                }
            }
        }

        if (albumsSearchRomaji != null) {
            for (album in albumsSearchRomaji!!) {
                if (album.toLowerCase().contains(query)) {
                    return true
                }
            }
        }

        if (artists != null) {
            for (artist in artists!!) {
                if (artist.toLowerCase().contains(query)) {
                    return true
                }
            }
        }

        if (artistsRomaji != null) {
            for (artist in artistsRomaji!!) {
                if (artist.toLowerCase().contains(query)) {
                    return true
                }
            }
        }

        if (artistsSearchRomaji != null) {
            for (artist in artistsSearchRomaji!!) {
                if (artist.toLowerCase().contains(query)) {
                    return true
                }
            }
        }

        return false
    }

    companion object {
        fun toSong(songListItem: SongListItem): Song {
            val albums = ArrayList<SongDescriptor>()
            for (i in 0 until songListItem.albums!!.size) {
                val albumDescriptor = SongDescriptor()
                albumDescriptor.id = songListItem.albumsId!![i]
                albumDescriptor.name = songListItem.albums!![i]
                albumDescriptor.nameRomaji = songListItem.albumsRomaji!![i]
                albumDescriptor.image = songListItem.albumsCover!![i]

                albums.add(albumDescriptor)
            }

            val artists = ArrayList<SongDescriptor>()
            for (i in 0 until songListItem.artists!!.size) {
                val artistDescriptor = SongDescriptor()
                artistDescriptor.id = songListItem.artistsId!![i]
                artistDescriptor.name = songListItem.artists!![i]
                artistDescriptor.nameRomaji = songListItem.artistsRomaji!![i]

                artists.add(artistDescriptor)
            }

            val sources = ArrayList<SongDescriptor>()
            for (i in 0 until songListItem.sources!!.size) {
                val sourceDescriptor = SongDescriptor()
                sourceDescriptor.name = songListItem.sources!![i]
                sourceDescriptor.nameRomaji = songListItem.sourcesRomaji!![i]

                sources.add(sourceDescriptor)
            }

            val song = Song()
            song.id = songListItem.id
            song.title = songListItem.title
            song.titleRomaji = songListItem.titleRomaji
            song.albums = albums
            song.artists = artists
            song.sources = sources
            song.duration = songListItem.duration
            song.favorite = songListItem.favorite
            song.enabled = songListItem.enabled

            return song
        }
    }
}
