package me.echeung.moemoekyun.client.socket.response;

import lombok.Getter;

@Getter
public class NotificationResponse extends BaseResponse {
    private String t;
    private Details d;

    @Getter
    public static class Details {
        private String type;
    }
}
