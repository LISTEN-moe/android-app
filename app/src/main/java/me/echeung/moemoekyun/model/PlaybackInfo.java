package me.echeung.moemoekyun.model;

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

    public String getAnimeName() {
        return animeName;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public int getListeners() {
        return listeners;
    }

    public List<BasicTrack> getPlayHistory() {
        return new ArrayList<>(Arrays.asList(last, secondLast));
    }

    public BasicTrack getLast() {
        return last;
    }

    public BasicTrack getSecondLast() {
        return secondLast;
    }

    public ExtendedInfo getExtended() {
        return extended;
    }

    public boolean hasExtended() {
        return this.extended != null;
    }

    public class ExtendedInfo {
        private boolean favorite;
        private QueueInfo queue;

        public ExtendedInfo(boolean favorite, QueueInfo queue) {
            this.favorite = favorite;
            this.queue = queue;
        }

        public boolean isFavorite() {
            return favorite;
        }

        public QueueInfo getQueue() {
            return queue;
        }
    }

    public class QueueInfo {
        private int songsInQueue;
        private boolean hasSongInQueue;
        private int inQueueBeforeUserSong;
        private int userSongsInQueue;

        public QueueInfo(int songsInQueue, boolean hasSongInQueue, int inQueueBeforeUserSong, int userSongsInQueue) {
            this.songsInQueue = songsInQueue;
            this.hasSongInQueue = hasSongInQueue;
            this.inQueueBeforeUserSong = inQueueBeforeUserSong;
            this.userSongsInQueue = userSongsInQueue;
        }

        public int getSongsInQueue() {
            return songsInQueue;
        }

        public boolean isHasSongInQueue() {
            return hasSongInQueue;
        }

        public int getInQueueBeforeUserSong() {
            return inQueueBeforeUserSong;
        }

        public int getUserSongsInQueue() {
            return userSongsInQueue;
        }
    }
}
