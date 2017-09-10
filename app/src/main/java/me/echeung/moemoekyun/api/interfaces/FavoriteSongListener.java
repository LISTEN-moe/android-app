package me.echeung.moemoekyun.api.interfaces;

public interface FavoriteSongListener {
    void onFailure(final String result);
    void onSuccess(final boolean favorited);
}
