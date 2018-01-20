package me.echeung.listenmoeapi.responses;

import java.util.List;

import lombok.Getter;
import me.echeung.listenmoeapi.models.ArtistSummary;

@Getter
public class ArtistsResponse extends BaseResponse {
    private List<ArtistSummary> artists;
}
