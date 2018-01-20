package me.echeung.listenmoeapi.callbacks;

import me.echeung.listenmoeapi.models.Artist;

public interface ArtistCallback extends BaseCallback {
    void onSuccess(final Artist artist);
}
