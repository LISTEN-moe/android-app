package me.echeung.moemoekyun.api.v3.model;

import java.util.List;

public class UserFavorites {

    private List<Song> songs;
    private SongsExtra extra;

    public UserFavorites(List<Song> songs, SongsExtra extra) {
        this.songs = songs;
        this.extra = extra;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public SongsExtra getExtra() {
        return extra;
    }

    public class SongsExtra {
        private int requests;

        public SongsExtra(int requests) {
            this.requests = requests;
        }

        public int getRequests() {
            return requests;
        }
    }
}
