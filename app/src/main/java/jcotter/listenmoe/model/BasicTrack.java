package jcotter.listenmoe.model;

import com.google.gson.annotations.SerializedName;

public class BasicTrack {
    @SerializedName("artist_name")
    private String artistName;
    @SerializedName("song_name")
    private String songName;

    public BasicTrack(String artistName, String songName) {
        this.artistName = artistName;
        this.songName = songName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }
}
