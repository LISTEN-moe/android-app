package me.echeung.listenmoeapi.models;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
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
    private String mListeners;
    private BasicTrack last;
    @SerializedName("second_last")
    private BasicTrack secondLast;
    private ExtendedInfo extended;

    public int getSongId() {
        return songId;
    }

    public String getAnimeName() {
        return animeName != null ? animeName.trim() : null;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public String getListeners() {
        return mListeners;
    }

    public void setListeners(String listeners) {
        this.mListeners = listeners;
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

    public static class PlaybackInfoDeserializer implements JsonDeserializer<PlaybackInfo> {

        @Override
        public PlaybackInfo deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            PlaybackInfo playbackInfo = new Gson().fromJson(json, PlaybackInfo.class);
            JsonObject jsonObject = json.getAsJsonObject();

            if (jsonObject.has("listeners")) {
                JsonElement elem = jsonObject.get("listeners");
                if (elem != null && !elem.isJsonNull()) {
                    playbackInfo.setListeners(elem.getAsString());
                }
            }

            return playbackInfo;
        }
    }
}
