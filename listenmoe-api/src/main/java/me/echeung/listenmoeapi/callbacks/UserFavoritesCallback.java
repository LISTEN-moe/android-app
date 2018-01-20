package me.echeung.listenmoeapi.callbacks;

import java.util.List;

import me.echeung.listenmoeapi.models.Song;

public interface UserFavoritesCallback extends BaseCallback {
    void onSuccess(final List<Song> favorites);
}
