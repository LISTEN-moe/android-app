package me.echeung.listenmoeapi.interfaces;

import java.util.List;

import me.echeung.listenmoeapi.models.Song;

public interface SearchListener {
    void onFailure(final String result);
    void onSuccess(final List<Song> favorites);
}
