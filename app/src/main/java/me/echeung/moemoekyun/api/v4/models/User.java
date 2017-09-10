package me.echeung.moemoekyun.api.v4.models;

import java.util.List;

public class User {
    private String uuid;
    private String email;
    private String username;
    private String avatarImage;
    private String bannerImage;
    private List<String> roles;
    private Object settings;
    private int uploadLimit;
    private int uploads;
    private int favorites;
}
