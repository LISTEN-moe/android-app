package me.echeung.moemoekyun.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
public class User {
    private String uuid;
    private String email;
    private String username;
    private String displayName;
    private String avatarImage;
    private String bannerImage;
    private String bio;
    private int additionalRequests;
    private int uploads;
    private int uploadLimit;
    private int favorites;

    @Setter
    private int requestsRemaining;
}
