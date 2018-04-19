package me.echeung.moemoekyun.api.callbacks;

import java.util.List;

import me.echeung.moemoekyun.model.ArtistSummary;

public interface ArtistsCallback extends BaseCallback {
    void onSuccess( List<ArtistSummary> artists);
}
