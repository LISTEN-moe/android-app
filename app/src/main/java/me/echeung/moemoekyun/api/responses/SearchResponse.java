package me.echeung.moemoekyun.api.responses;

import java.util.List;

import me.echeung.moemoekyun.api.models.Song;

public class SearchResponse extends BaseResponse {
    private List<Song> songs;

    public List<Song> getSongs() {
        return songs;
    }
}
