package me.echeung.moemoekyun.interfaces;

import me.echeung.moemoekyun.model.SongsList;

public interface UserFavoritesListener {
    void onFailure(final String result);
    void onSuccess(final SongsList songsList);
}
