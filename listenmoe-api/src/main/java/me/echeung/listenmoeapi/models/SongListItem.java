package me.echeung.listenmoeapi.models;

import java.util.List;

import lombok.Getter;

@Getter
public class SongListItem {
    private List<String> albums;
    private List<String> albumsCover;
    private List<String> albumsReleaseDate;
    private List<String> albumsSearchRomaji;
    private List<Integer> albumsTrackNumber;
    private List<Integer> albumsType;
    private List<String> artists;
    private List<Integer> artistsId;
    private List<String> artistsRomaji;
    private List<String> artistsSearchRomaji;
    private int duration;
    private boolean enabled;
    private boolean favorite;
    private List<String> groups;
    private List<Integer> groupsId;
    private List<String> groupsRomaji;
    private List<String> groupsSearchRomaji;
    private int id;
    private String lastPlayed;
    private String snippet;
    private List<String> sources;
    private List<String> sourcesRomaji;
    private List<String> tags;
    private String title;
    private String titleSearchRomaji;
    private String uploaderUuid;
    private String uploaderUsername;
    private String uploaderDisplayName;
}
