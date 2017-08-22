package me.echeung.moemoekyun.api.v3.interfaces;

import java.util.List;

import me.echeung.moemoekyun.api.v3.model.Song;

public interface SearchListener {
    void onFailure(final String result);
    void onSuccess(final List<Song> favorites);
}
