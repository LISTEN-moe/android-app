package me.echeung.moemoekyun.api.interfaces;

import me.echeung.moemoekyun.api.models.UserFavorites;

public interface UserFavoritesListener {
    void onFailure(final String result);
    void onSuccess(final UserFavorites userFavorites);
}
