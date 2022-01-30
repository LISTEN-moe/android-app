package me.echeung.moemoekyun.client.api

enum class Library(val socketUrl: String, val streamUrl: String) {
    jpop(
        "wss://listen.moe/gateway_v2",
        "https://listen.moe/fallback"
    ),
    kpop(
        "wss://listen.moe/kpop/gateway_v2",
        "https://listen.moe/kpop/fallback"
    ),
    ;

    companion object {
        const val API_BASE = "https://listen.moe/graphql"

        const val CDN_ALBUM_ART_URL = "https://cdn.listen.moe/covers/"
        const val CDN_AVATAR_URL = "https://cdn.listen.moe/avatars/"
        const val CDN_BANNER_URL = "https://cdn.listen.moe/banners/"
    }
}
