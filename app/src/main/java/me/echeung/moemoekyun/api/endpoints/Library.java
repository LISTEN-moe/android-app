package me.echeung.moemoekyun.api.endpoints;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class Library {

    public static final String API_BASE = "https://listen.moe/api/";

    public static final String CDN_ALBUM_ART_URL = "https://cdn.listen.moe/covers/";
    public static final String CDN_AVATAR_URL = "https://cdn.listen.moe/avatars/";
    public static final String CDN_BANNER_URL = "https://cdn.listen.moe/banners/";

    @Getter
    private final String name;
    @Getter
    private final String socketUrl;
    @Getter
    private final String streamUrl;

}
