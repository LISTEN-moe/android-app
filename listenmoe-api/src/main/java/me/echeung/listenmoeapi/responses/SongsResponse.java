package me.echeung.listenmoeapi.responses;

import java.util.List;

import lombok.Getter;
import me.echeung.listenmoeapi.models.SongListItem;

@Getter
public class SongsResponse extends BaseResponse {
    private List<SongListItem> songs;
}
