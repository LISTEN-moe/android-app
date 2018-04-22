package me.echeung.moemoekyun.client.socket.response;

import lombok.Getter;
import me.echeung.moemoekyun.client.model.User;

@Getter
public class ConnectResponse extends BaseResponse {
    private Details d;

    @Getter
    public static class Details {
        private int heartbeat;
        private String message;
        private User user;
    }
}
