package me.echeung.moemoekyun.api.responses;

import java.util.List;

import lombok.Getter;
import me.echeung.moemoekyun.models.ArtistSummary;

@Getter
public class ArtistsResponse extends BaseResponse {
    private List<ArtistSummary> artists;
}
