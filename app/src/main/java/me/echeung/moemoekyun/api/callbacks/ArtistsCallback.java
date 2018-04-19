package me.echeung.moemoekyun.api.callbacks;

import java.util.List;

import me.echeung.moemoekyun.api.models.ArtistSummary;

public interface ArtistsCallback extends BaseCallback {
    void onSuccess(final List<ArtistSummary> artists);
}
