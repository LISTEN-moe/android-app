package jcotter.listenmoe.model;

import java.util.List;

public class SongsList {
    private List<Song> songs;
//    private List<Integer> extra;

    public SongsList(List<Song> songs, List<Integer> extra) {
        this.songs = songs;
//        this.extra = extra;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

//    public List<Integer> getExtra() {
//        return extra;
//    }
//
//    public void setExtra(List<Integer> extra) {
//        this.extra = extra;
//    }
}
