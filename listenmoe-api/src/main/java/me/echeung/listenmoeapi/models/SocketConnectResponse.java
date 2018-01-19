package me.echeung.listenmoeapi.models;

import lombok.Getter;

public class SocketConnectResponse extends SocketBaseResponse {

    @Getter
    private Details d;

    public class Details {

        @Getter
        private int heartbeat;

        @Getter
        private String message;

    }

}
