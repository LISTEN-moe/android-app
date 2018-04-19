package me.echeung.moemoekyun.api.callbacks;

import java.util.List;

import me.echeung.moemoekyun.models.Song;

public interface SearchCallback extends BaseCallback {
    void onSuccess(List<Song> favorites);
}
