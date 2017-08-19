package me.echeung.moemoekyun.api.old.interfaces;

import java.util.List;

import me.echeung.moemoekyun.api.old.model.Song;

public interface SearchListener {
    void onFailure(final String result);
    void onSuccess(final List<Song> favorites);
}
