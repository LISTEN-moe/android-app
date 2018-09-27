package me.echeung.moemoekyun.client.api.library;

public abstract class Library {

    public static final String API_BASE = "https://listen.moe/api/";

    public static final String CDN_ALBUM_ART_URL = "https://cdn.listen.moe/covers/";
    public static final String CDN_AVATAR_URL = "https://cdn.listen.moe/avatars/";
    public static final String CDN_BANNER_URL = "https://cdn.listen.moe/banners/";

    private final String name;
    private final String socketUrl;
    private final String streamUrl;

    public Library(String name, String socketUrl, String streamUrl) {
        this.name = name;
        this.socketUrl = socketUrl;
        this.streamUrl = streamUrl;
    }

    public String getName() {
        return name;
    }

    public String getSocketUrl() {
        return socketUrl;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

}
