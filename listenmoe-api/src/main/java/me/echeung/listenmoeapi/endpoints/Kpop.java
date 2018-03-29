package me.echeung.listenmoeapi.endpoints;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Kpop extends Library {

    public static final Library INSTANCE = new Kpop();

    public static final String SOCKET = "wss://listen.moe/kpop/gateway";
    public static final String STREAM_MP3 = "https://listen.moe/kpop/fallback";

}
