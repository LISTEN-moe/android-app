package me.echeung.moemoekyun.api.interfaces;

import java.util.List;

import me.echeung.moemoekyun.api.models.Song;

public interface SearchListener {
    void onFailure(final String result);
    void onSuccess(final List<Song> favorites);
}
