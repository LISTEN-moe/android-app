package me.echeung.moemoekyun.api.callbacks;

import java.util.List;

import me.echeung.moemoekyun.api.models.Song;

public interface SearchCallback extends BaseCallback {
    void onSuccess(final List<Song> favorites);
}
