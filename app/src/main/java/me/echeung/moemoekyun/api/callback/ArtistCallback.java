package me.echeung.moemoekyun.api.callback;

import me.echeung.moemoekyun.model.Artist;

public interface ArtistCallback extends BaseCallback {
    void onSuccess( Artist artist);
}
