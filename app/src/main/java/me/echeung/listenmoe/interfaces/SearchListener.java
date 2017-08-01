package me.echeung.listenmoe.interfaces;

import java.util.List;

import me.echeung.listenmoe.model.Song;

public interface SearchListener {
    void onFailure(final String result);

    void onSuccess(final List<Song> favorites);
}
