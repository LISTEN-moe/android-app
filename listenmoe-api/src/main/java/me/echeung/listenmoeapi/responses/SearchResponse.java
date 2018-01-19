package me.echeung.listenmoeapi.responses;

import java.util.List;

import me.echeung.listenmoeapi.models.Song;

public class SearchResponse extends BaseResponse {
    private List<Song> songs;

    public List<Song> getSongs() {
        return songs;
    }
}
