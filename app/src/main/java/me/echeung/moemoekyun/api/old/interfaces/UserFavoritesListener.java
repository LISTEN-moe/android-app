package me.echeung.moemoekyun.api.old.interfaces;

import me.echeung.moemoekyun.api.old.model.UserFavorites;

public interface UserFavoritesListener {
    void onFailure(final String result);
    void onSuccess(final UserFavorites userFavorites);
}
