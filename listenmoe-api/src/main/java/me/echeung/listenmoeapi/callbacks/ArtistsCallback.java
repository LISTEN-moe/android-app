package me.echeung.listenmoeapi.callbacks;

import java.util.List;

import me.echeung.listenmoeapi.models.ArtistSummary;

public interface ArtistsCallback extends BaseCallback {
    void onSuccess(final List<ArtistSummary> artists);
}
