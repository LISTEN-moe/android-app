package me.echeung.listenmoeapi.models;

import java.util.List;

import lombok.Getter;

public class SocketUpdateResponse extends SocketBaseResponse {

    @Getter
    private String t;

    @Getter
    private Details d;

    public class Details {

        @Getter
        private String event;

        @Getter
        private String startTime;

        @Getter
        private int listeners;

        @Getter
        private String requester;

        @Getter
        private List<Track> lastPlayed;

        @Getter
        private Track song;

    }

}
