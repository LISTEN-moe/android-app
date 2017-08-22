package me.echeung.moemoekyun.api.v3.interfaces;

import me.echeung.moemoekyun.api.v3.model.UserFavorites;

public interface UserFavoritesListener {
    void onFailure(final String result);
    void onSuccess(final UserFavorites userFavorites);
}
