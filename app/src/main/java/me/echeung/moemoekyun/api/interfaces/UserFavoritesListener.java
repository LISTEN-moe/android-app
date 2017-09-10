package me.echeung.moemoekyun.api.interfaces;

import me.echeung.moemoekyun.api.responses.UserFavoritesResponse;

public interface UserFavoritesListener {
    void onFailure(final String result);
    void onSuccess(final UserFavoritesResponse userFavorites);
}
