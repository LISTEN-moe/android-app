package jcotter.listenmoe.model;

import java.util.List;

public class SongsList {
    private List<Song> songs;
    private SongsExtra extra;

    public SongsList(List<Song> songs, SongsExtra extra) {
        this.songs = songs;
        this.extra = extra;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public SongsExtra getExtra() {
        return extra;
    }

    public void setExtra(SongsExtra extra) {
        this.extra = extra;
    }
}
