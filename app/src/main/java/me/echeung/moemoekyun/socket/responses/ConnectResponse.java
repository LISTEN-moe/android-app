package me.echeung.moemoekyun.socket.responses;

import lombok.Getter;
import me.echeung.moemoekyun.models.User;

@Getter
public class ConnectResponse extends BaseResponse {
    private Details d;

    @Getter
    public class Details {
        private int heartbeat;
        private String message;
        private User user;
    }
}
