package me.echeung.listenmoeapi.models;

import lombok.Builder;
import lombok.Getter;

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
    private int requestsRemaining;
    private int additionalRequests;
    private int uploads;
    private int uploadLimit;
    private int favorites;
}
