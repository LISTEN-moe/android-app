package me.echeung.listenmoeapi.responses.socket;

import java.util.List;

import lombok.Getter;
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
        private Event event;
    }

    @Getter
    public class Queue {
        private int inQueue;
        private int inQueueByUser;
        private int inQueueBeforeUser;
    }

}
