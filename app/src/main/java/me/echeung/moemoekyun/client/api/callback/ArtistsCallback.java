package me.echeung.moemoekyun.client.api.callback;

import java.util.List;

import me.echeung.moemoekyun.client.model.ArtistSummary;

public interface ArtistsCallback extends BaseCallback {
    void onSuccess( List<ArtistSummary> artists);
}
