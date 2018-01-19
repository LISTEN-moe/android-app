package me.echeung.listenmoeapi.responses;

import java.util.List;

import me.echeung.listenmoeapi.models.Track;

public class SearchResponse extends BaseResponse {
    private List<Track> songs;

    public List<Track> getSongs() {
        return songs;
    }
}
