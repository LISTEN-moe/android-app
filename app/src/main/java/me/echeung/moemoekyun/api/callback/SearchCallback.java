package me.echeung.moemoekyun.api.callback;

import java.util.List;

import me.echeung.moemoekyun.model.Song;

public interface SearchCallback extends BaseCallback {
    void onSuccess(List<Song> favorites);
}
