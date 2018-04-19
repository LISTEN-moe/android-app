package me.echeung.moemoekyun.api.responses;

import java.util.List;

import lombok.Getter;
import me.echeung.moemoekyun.api.models.Song;

@Getter
public class SearchResponse extends BaseResponse {
    private List<Song> songs;
}
