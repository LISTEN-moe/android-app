package me.echeung.moemoekyun.api.models;

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
