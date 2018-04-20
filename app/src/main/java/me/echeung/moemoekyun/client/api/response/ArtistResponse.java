package me.echeung.moemoekyun.client.api.response;

import lombok.Getter;
import me.echeung.moemoekyun.client.model.Artist;

@Getter
public class ArtistResponse extends BaseResponse {
    private Artist artist;
}
