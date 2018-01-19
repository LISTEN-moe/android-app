package me.echeung.listenmoeapi.models;

import java.util.List;

import lombok.Getter;

@Getter
public class SocketUpdateResponse extends SocketBaseResponse {

    private String t;

    private Details d;

    @Getter
    public class Details {

        private String event;

        private String startTime;

        private int listeners;

        private User requester;

        private List<Song> lastPlayed;

        private Song song;

        private Queue queue;

    }

    @Getter
    public class Queue {

        private int inQueue;

        private int inQueueByUser;

        private int inQueueBeforeUser;

    }

}
