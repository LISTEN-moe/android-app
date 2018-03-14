package me.echeung.listenmoeapi.responses.socket;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import me.echeung.listenmoeapi.models.Event;
import me.echeung.listenmoeapi.models.Song;
import me.echeung.listenmoeapi.models.User;

@Getter
public class SocketUpdateResponse extends SocketBaseResponse {
    private String t;
    private Details d;

    @Getter
    public class Details {
        private Song song;
        private String startTime;
        private List<Song> lastPlayed;
        private Queue queue;
        private int listeners;
        private User requester;
        @Setter
        private Event event;
    }

    @Getter
    public class Queue {
        private int inQueue;
        private int inQueueByUser;
        private int inQueueBeforeUser;
    }

    // TODO: remove this once we're safely at a point where the app won't be handling both
    // string and object events
    public static class DetailsDeserializer implements JsonDeserializer<Details> {

        @Override
        public Details deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            Details details = new Gson().fromJson(json, Details.class);
            JsonObject jsonObject = json.getAsJsonObject();

            if (jsonObject.has("event")) {
                JsonElement elem = jsonObject.get("event");
                if (elem != null && elem.isJsonPrimitive()) {
                    details.setEvent(new Event(elem.getAsString(), null));
                }
            }

            return details;
        }
    }
}
