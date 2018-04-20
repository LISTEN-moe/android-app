package me.echeung.moemoekyun.client.api.callback;

import java.util.List;

import me.echeung.moemoekyun.client.model.Song;

public interface SearchCallback extends BaseCallback {
    void onSuccess(List<Song> favorites);
}
