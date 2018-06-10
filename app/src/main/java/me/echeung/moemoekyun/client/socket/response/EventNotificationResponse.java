package me.echeung.moemoekyun.client.socket.response;

import lombok.Getter;
import me.echeung.moemoekyun.client.model.Event;

@Getter
public class EventNotificationResponse extends NotificationResponse {
    public static final String TYPE = "EVENT";

    private Details d;

    @Getter
    public static class Details extends NotificationResponse.Details {
        private Event event;
    }
}
