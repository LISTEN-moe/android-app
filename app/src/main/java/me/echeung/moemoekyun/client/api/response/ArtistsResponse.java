package me.echeung.moemoekyun.client.api.response;

import java.util.List;

import lombok.Getter;
import me.echeung.moemoekyun.client.model.ArtistSummary;

@Getter
public class ArtistsResponse extends BaseResponse {
    private List<ArtistSummary> artists;
}
