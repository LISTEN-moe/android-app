package me.echeung.listenmoeapi.models;

import java.util.List;

import lombok.Getter;

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
        private String event;
    }

    @Getter
    public class Queue {
        private int inQueue;
        private int inQueueByUser;
        private int inQueueBeforeUser;
    }

}
