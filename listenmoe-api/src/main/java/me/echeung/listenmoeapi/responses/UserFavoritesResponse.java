package me.echeung.listenmoeapi.responses;

import java.util.List;

import me.echeung.listenmoeapi.models.Song;

public class UserFavoritesResponse extends BaseResponse {
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
