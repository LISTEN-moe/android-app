package me.echeung.moemoekyun.interfaces;

import java.util.List;

import me.echeung.moemoekyun.model.Song;

public interface SearchListener {
    void onFailure(final String result);
    void onSuccess(final List<Song> favorites);
}
