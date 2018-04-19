package me.echeung.moemoekyun.api.endpoints;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Kpop extends Library {

    public static final Library INSTANCE = new Kpop();

    public static final String NAME = "kpop";

    public String getName() {
        return NAME;
    }

    public String getSocketUrl() {
        return "wss://listen.moe/kpop/gateway";
    }

    public String getStreamUrl() {
        return "https://listen.moe/kpop/fallback";
    }

}
