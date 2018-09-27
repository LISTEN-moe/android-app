package me.echeung.moemoekyun.client.model

class Artist {
    var albums: List<Album>? = null
    var id: Int = 0
    var image: String? = null
    var lastPlayed: String? = null
    var name: String? = null
    var nameRomaji: String? = null
    var played: Long = 0
    var slug: String? = null
    var songs: List<Any>? = null

    class Album {
        var artistAlbum: ArtistAlbum? = null
        var id: Int = 0
        var image: String? = null
        var name: String? = null
        var nameRomaji: String? = null
        var releaseDate: String? = null
        var type: Int = 0

        inner class ArtistAlbum {
            var artistId: Int = 0
            var albumId: Int = 0
        }
    }

    class Song {
        var albums: List<AlbumSummary>? = null
        var artistSong: ArtistSong? = null
        var artists: List<ArtistSummary>? = null
        var duration: Int = 0
        var id: Int = 0
        var lastPlayed: String? = null
        var snippet: String? = null
        var title: String? = null
        var titleRomaji: String? = null
        var uploader: User? = null

        class AlbumSummary {
            var albumId: Int = 0
            var songId: Int = 0
            var trackNumber: Int = 0
        }

        class ArtistSong {
            var artistId: Int = 0
            var songId: Int = 0
        }

        class ArtistSummary {
            var artistSong: ArtistSong? = null
            var id: Int = 0
            var name: String? = null
            var nameRomaji: String? = null
        }
    }
}
