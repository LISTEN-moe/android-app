package me.echeung.listenmoeapi.responses;

import java.util.List;

import me.echeung.listenmoeapi.models.Track;

public class UserFavoritesResponse extends BaseResponse {
    private List<Track> songs;
    private SongsExtra extra;

    public List<Track> getSongs() {
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
