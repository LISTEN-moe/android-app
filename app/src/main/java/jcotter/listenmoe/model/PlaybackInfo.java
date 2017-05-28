package jcotter.listenmoe.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlaybackInfo extends BasicTrack {
    @SerializedName("song_id")
    private int songId;
    @SerializedName("anime_name")
    private String animeName;
    @SerializedName("requested_by")
    private String requestedBy;
    private int listeners;
    private BasicTrack last;
    @SerializedName("second_last")
    private BasicTrack secondLast;
    private ExtendedInfo extended;

    public PlaybackInfo(String artistName, String songName, int songId, String animeName, String requestedBy, int listeners, BasicTrack last, BasicTrack secondLast) {
        super(artistName, songName);
        this.songId = songId;
        this.animeName = animeName;
        this.requestedBy = requestedBy;
        this.listeners = listeners;
        this.last = last;
        this.secondLast = secondLast;
    }

    public PlaybackInfo(String artistName, String songName, int songId, String animeName, String requestedBy, int listeners, BasicTrack last, BasicTrack secondLast, ExtendedInfo extended) {
        super(artistName, songName);
        this.songId = songId;
        this.animeName = animeName;
        this.requestedBy = requestedBy;
        this.listeners = listeners;
        this.last = last;
        this.secondLast = secondLast;
        this.extended = extended;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public String getAnimeName() {
        return animeName;
    }

    public void setAnimeName(String animeName) {
        this.animeName = animeName;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public int getListeners() {
        return listeners;
    }

    public void setListeners(int listeners) {
        this.listeners = listeners;
    }

    public List<BasicTrack> getPlayHistory() {
        return new ArrayList<>(Arrays.asList(last, secondLast));
    }

    public BasicTrack getLast() {
        return last;
    }

    public void setLast(BasicTrack last) {
        this.last = last;
    }

    public BasicTrack getSecondLast() {
        return secondLast;
    }

    public void setSecondLast(BasicTrack secondLast) {
        this.secondLast = secondLast;
    }

    public ExtendedInfo getExtended() {
        return extended;
    }

    public void setExtended(ExtendedInfo extended) {
        this.extended = extended;
    }

    public boolean hasExtended() {
        return this.extended != null;
    }
}
