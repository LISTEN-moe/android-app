package me.echeung.listenmoeapi.callbacks;

public interface FavoriteSongCallback extends BaseCallback {
    void onSuccess(final boolean favorited);
}
