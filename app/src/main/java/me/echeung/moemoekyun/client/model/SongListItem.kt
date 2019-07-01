package me.echeung.moemoekyun.client.model

data class SongListItem(
    var id: Int = 0,
    var title: String? = null,
    var titleRomaji: String? = null,
    var titleSearchRomaji: String? = null,
    var artists: List<String>? = null,
    var artistsId: List<Int>? = null,
    var artistsRomaji: List<String>? = null,
    var artistsSearchRomaji: List<String>? = null,
    var albums: List<String>? = null,
    var albumsId: List<Int>? = null,
    var albumsCover: List<String>? = null,
    var albumsRomaji: List<String>? = null,
    var albumsSearchRomaji: List<String>? = null,
    var sources: List<String>? = null,
    var sourcesRomaji: List<String>? = null,
    var duration: Int = 0,
    var favorite: Boolean = false
) {

    fun search(query: String?): Boolean {
        if (query.isNullOrBlank()) {
            return true
        }

        return title.orEmpty().contains(query, ignoreCase = true) ||
                titleRomaji.orEmpty().contains(query, ignoreCase = true) ||
                titleSearchRomaji.orEmpty().contains(query, ignoreCase = true) ||
                artists.orEmpty().any { it.contains(query, ignoreCase = true) } ||
                artistsRomaji.orEmpty().any { it.contains(query, ignoreCase = true) } ||
                artistsSearchRomaji.orEmpty().any { it.contains(query, ignoreCase = true) } ||
                albums.orEmpty().any { it.contains(query, ignoreCase = true) } ||
                albumsRomaji.orEmpty().any { it.contains(query, ignoreCase = true) } ||
                albumsSearchRomaji.orEmpty().any { it.contains(query, ignoreCase = true) }
    }

    companion object {
        fun toSong(songListItem: SongListItem): Song {
            val songAlbums = songListItem.albums.orEmpty()
                    .mapIndexed { i, _ -> SongDescriptor(
                            id = songListItem.albumsId!![i],
                            name = songListItem.albums!![i],
                            nameRomaji = songListItem.albumsRomaji!![i],
                            image = songListItem.albumsCover!![i])
                    }

            val songArtists = songListItem.artists.orEmpty()
                    .mapIndexed { i, _ -> SongDescriptor(
                            id = songListItem.artistsId!![i],
                            name = songListItem.artists!![i],
                            nameRomaji = songListItem.artistsRomaji!![i])
                    }

            val songSources = songListItem.sources.orEmpty()
                    .mapIndexed { i, _ -> SongDescriptor(
                            name = songListItem.sources!![i],
                            nameRomaji = songListItem.sourcesRomaji!![i])
                    }

            return Song().apply {
                id = songListItem.id
                title = songListItem.title
                titleRomaji = songListItem.titleRomaji
                albums = songAlbums
                artists = songArtists
                sources = songSources
                duration = songListItem.duration
                favorite = songListItem.favorite
            }
        }
    }
}
