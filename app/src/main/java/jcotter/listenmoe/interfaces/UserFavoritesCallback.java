package jcotter.listenmoe.interfaces;

import java.util.List;

import jcotter.listenmoe.model.Song;

public interface UserFavoritesCallback {
    void onFailure(final String result);

    void onSuccess(final List<Song> favorites);
}
