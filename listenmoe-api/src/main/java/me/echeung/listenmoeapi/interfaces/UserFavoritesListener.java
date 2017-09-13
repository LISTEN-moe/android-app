package me.echeung.listenmoeapi.interfaces;

import me.echeung.listenmoeapi.responses.UserFavoritesResponse;

public interface UserFavoritesListener {
    void onFailure(final String result);
    void onSuccess(final UserFavoritesResponse userFavorites);
}
