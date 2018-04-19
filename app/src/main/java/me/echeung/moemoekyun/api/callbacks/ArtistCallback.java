package me.echeung.moemoekyun.api.callbacks;

import me.echeung.moemoekyun.models.Artist;

public interface ArtistCallback extends BaseCallback {
    void onSuccess( Artist artist);
}
