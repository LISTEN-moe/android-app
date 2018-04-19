package me.echeung.moemoekyun.api.responses.socket;

import lombok.Getter;
import me.echeung.moemoekyun.api.models.User;

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
