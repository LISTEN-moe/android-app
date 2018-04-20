package me.echeung.moemoekyun.socket.response;

import java.util.List;

import lombok.Getter;
import me.echeung.moemoekyun.model.Event;
import me.echeung.moemoekyun.model.Song;
import me.echeung.moemoekyun.model.User;

@Getter
public class UpdateResponse extends BaseResponse {
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
