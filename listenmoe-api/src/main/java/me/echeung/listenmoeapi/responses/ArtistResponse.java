package me.echeung.listenmoeapi.responses;

import lombok.Getter;
import me.echeung.listenmoeapi.models.Artist;

@Getter
public class ArtistResponse extends BaseResponse {
    private Artist artist;
}
