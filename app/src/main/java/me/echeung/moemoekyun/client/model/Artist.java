package me.echeung.moemoekyun.client.model;

import java.util.List;

import lombok.Getter;

@Getter
public class Artist {
    private List<Album> albums;
    private int id;
    private String image;
    private String lastPlayed;
    private String name;
    private String nameRomaji;
    private long played;
    private String slug;
    private List<Object> songs;

    @Getter
    public static class Album {
        private ArtistAlbum artistAlbum;
        private int id;
        private String image;
        private String name;
        private String nameRomaji;
        private String releaseDate;
        private int type;

        @Getter
        public class ArtistAlbum {
            private int artistId;
            private int albumId;
        }
    }

    @Getter
    public static class Song {
        private List<AlbumSummary> albums;
        private ArtistSong artistSong;
        private List<ArtistSummary> artists;
        private int duration;
        private int id;
        private String lastPlayed;
        private String snippet;
        private String title;
        private String titleRomaji;
        private User uploader;

        @Getter
        public static class AlbumSummary {
            private int albumId;
            private int songId;
            private int trackNumber;
        }

        @Getter
        public static class ArtistSong {
            private int artistId;
            private int songId;
        }

        @Getter
        public static class ArtistSummary {
            private ArtistSong artistSong;
            private int id;
            private String name;
            private String nameRomaji;
        }
    }
}
