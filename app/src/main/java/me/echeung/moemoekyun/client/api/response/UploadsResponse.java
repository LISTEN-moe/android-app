package me.echeung.moemoekyun.client.api.response;

import java.util.List;

import lombok.Getter;
import me.echeung.moemoekyun.client.model.Song;

@Getter
public class UploadsResponse extends BaseResponse {
    private List<Song> uploads;
}
