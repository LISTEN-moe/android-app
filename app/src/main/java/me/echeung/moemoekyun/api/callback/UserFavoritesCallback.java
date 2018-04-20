package me.echeung.moemoekyun.api.callback;

import java.util.List;

import me.echeung.moemoekyun.model.Song;

public interface UserFavoritesCallback extends BaseCallback {
    void onSuccess( List<Song> favorites);
}
