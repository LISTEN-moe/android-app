package me.echeung.moemoekyun.api.v3.interfaces;

public interface FavoriteSongListener {
    void onFailure(final String result);
    void onSuccess(final boolean favorited);
}
