package me.echeung.moemoekyun.api.models;

import lombok.Getter;

@Getter
public class ArtistSummary {
    private int id;
    private String image;
    private String name;
    private String nameRomaji;
    private int releaseCount;
    private String slug;
    private int songCount;
}
