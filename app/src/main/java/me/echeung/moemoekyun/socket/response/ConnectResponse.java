package me.echeung.moemoekyun.socket.response;

import lombok.Getter;
import me.echeung.moemoekyun.model.User;

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
