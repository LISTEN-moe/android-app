package jcotter.listenmoe.interfaces;

import jcotter.listenmoe.model.SongsList;

public interface UserFavoritesCallback {
    void onFailure(final String result);

    void onSuccess(final SongsList songsList);
}
