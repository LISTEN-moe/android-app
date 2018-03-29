package me.echeung.listenmoeapi.endpoints;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Jpop extends Library {

    public static final Library INSTANCE = new Jpop();

    public static final String SOCKET = "wss://listen.moe/gateway";
    public static final String STREAM_MP3 = "https://listen.moe/fallback";

}
