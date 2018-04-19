package me.echeung.moemoekyun.api.endpoints;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Jpop extends Library {

    public static final Library INSTANCE = new Jpop();

    public static final String NAME = "jpop";

    public String getName() {
        return NAME;
    }

    public String getSocketUrl() {
        return "wss://listen.moe/gateway";
    }

    public String getStreamUrl() {
        return "https://listen.moe/fallback";
    }

}
