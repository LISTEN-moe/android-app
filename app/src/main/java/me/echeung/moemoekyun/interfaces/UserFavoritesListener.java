package me.echeung.moemoekyun.interfaces;

import me.echeung.moemoekyun.model.UserFavorites;

public interface UserFavoritesListener {
    void onFailure(final String result);
    void onSuccess(final UserFavorites userFavorites);
}
