package me.echeung.moemoekyun.api.response;

import java.util.List;

import lombok.Getter;
import me.echeung.moemoekyun.model.SongListItem;

@Getter
public class SongsResponse extends BaseResponse {
    private List<SongListItem> songs;
}
