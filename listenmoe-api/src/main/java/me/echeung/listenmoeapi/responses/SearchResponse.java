package me.echeung.listenmoeapi.responses;

import java.util.List;

import lombok.Getter;
import me.echeung.listenmoeapi.models.Song;

@Getter
public class SearchResponse extends BaseResponse {
    private List<Song> songs;
}
