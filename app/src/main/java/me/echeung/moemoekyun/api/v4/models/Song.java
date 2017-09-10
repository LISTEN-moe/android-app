package me.echeung.moemoekyun.api.v4.models;

import java.util.List;

public class Song {
    private int id;
    private String title;
    private String source;
    private int duration;
    private String lastPlayed;
    private List<Artist> artists;
    private List<Album> albums;
    private boolean favorite;
}
