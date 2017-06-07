package jcotter.listenmoe.interfaces;

import jcotter.listenmoe.model.SongsList;

public interface UserFavoritesListener {
    void onFailure(final String result);

    void onSuccess(final SongsList songsList);
}
