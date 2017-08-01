package me.echeung.listenmoe.interfaces;

import me.echeung.listenmoe.model.SongsList;

public interface UserFavoritesListener {
    void onFailure(final String result);

    void onSuccess(final SongsList songsList);
}
