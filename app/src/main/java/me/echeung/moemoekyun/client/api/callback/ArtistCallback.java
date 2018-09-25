package me.echeung.moemoekyun.client.api.callback;

import me.echeung.moemoekyun.client.model.Artist;

public interface ArtistCallback extends BaseCallback {
    void onSuccess(Artist artist);
}
