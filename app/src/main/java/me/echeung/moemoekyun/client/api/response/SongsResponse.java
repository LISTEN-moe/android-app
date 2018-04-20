package me.echeung.moemoekyun.client.api.response;

import java.util.List;

import lombok.Getter;
import me.echeung.moemoekyun.client.model.SongListItem;

@Getter
public class SongsResponse extends BaseResponse {
    private List<SongListItem> songs;
}
