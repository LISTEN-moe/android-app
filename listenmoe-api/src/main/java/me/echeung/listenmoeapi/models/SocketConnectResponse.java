package me.echeung.listenmoeapi.models;

import lombok.Getter;

@Getter
public class SocketConnectResponse extends SocketBaseResponse {

    private Details d;

    @Getter
    public class Details {
        private int heartbeat;
        private String message;
        private User user;
    }

}
