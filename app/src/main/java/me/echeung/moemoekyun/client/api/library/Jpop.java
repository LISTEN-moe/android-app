package me.echeung.moemoekyun.client.api.library;

public class Jpop extends Library {

    public static final Library INSTANCE = new Jpop();

    public static final String NAME = "jpop";

    private Jpop() {
        super(NAME, "wss://listen.moe/gateway", "https://listen.moe/fallback");
    }

}
