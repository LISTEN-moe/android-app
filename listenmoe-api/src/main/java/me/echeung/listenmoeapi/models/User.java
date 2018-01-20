package me.echeung.listenmoeapi.models;

import lombok.Getter;

@Getter
public class User {
    private String uuid;
    private String email;
    private String username;
    private String displayName;
    private int uploads;
}
