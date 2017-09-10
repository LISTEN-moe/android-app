package me.echeung.moemoekyun.api.models;

import java.util.List;

public class UserFavorites {

    private List<Song> songs;
    private SongsExtra extra;

    public List<Song> getSongs() {
        return songs;
    }

    public SongsExtra getExtra() {
        return extra;
    }

    public class SongsExtra {
        private int requests;

        public int getRequests() {
            return requests;
        }
    }
}
