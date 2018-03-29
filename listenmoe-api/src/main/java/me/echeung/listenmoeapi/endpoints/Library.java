package me.echeung.listenmoeapi.endpoints;

public abstract class Library {

    public static final String API_BASE = "https://listen.moe/api/";

    public static final String SOCKET = "wss://listen.moe/gateway";

//    public static final String STREAM_VORBIS = "https://listen.moe/stream";
//    public static final String STREAM_OPUS = "https://listen.moe/opus";
    public static final String STREAM_MP3 = "https://listen.moe/fallback";

    public static final String CDN_ALBUM_ART_URL = "https://cdn.listen.moe/covers/";
    public static final String CDN_AVATAR_URL = "https://cdn.listen.moe/avatars/";
    public static final String CDN_BANNER_URL = "https://cdn.listen.moe/banners/";

}
