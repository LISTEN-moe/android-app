package me.echeung.moemoekyun.api.response;

import lombok.Getter;
import me.echeung.moemoekyun.model.Artist;

@Getter
public class ArtistResponse extends BaseResponse {
    private Artist artist;
}
