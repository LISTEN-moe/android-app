package me.echeung.listenmoeapi.callbacks;

import me.echeung.listenmoeapi.responses.UserFavoritesResponse;

public interface UserFavoritesCallback extends BaseCallback {
    void onSuccess(final UserFavoritesResponse userFavorites);
}
