package me.echeung.listenmoeapi.models;

import com.google.gson.annotations.SerializedName;

public class BasicTrack {

    @SerializedName("artist_name")
    private String artistName;
    @SerializedName("song_name")
    private String songName;

    public String getArtistName() {
        return artistName != null ? artistName.trim() : null;
    }

    public String getSongName() {
        return songName != null ? songName.trim() : null;
    }

    @Override
    public String toString() {
        return songName + " - " + artistName;
    }
}
