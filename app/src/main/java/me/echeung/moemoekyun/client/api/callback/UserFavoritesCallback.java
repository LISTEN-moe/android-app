package me.echeung.moemoekyun.client.api.callback;

import java.util.List;

import me.echeung.moemoekyun.client.model.Song;

public interface UserFavoritesCallback extends BaseCallback {
    void onSuccess( List<Song> favorites);
}
