package me.echeung.moemoekyun.client.api

enum class Library(val id: String, val socketUrl: String, val streamUrl: String) {
    Jpop(
        "jpop",
        "wss://listen.moe/gateway_v2",
        "https://listen.moe/fallback"
    ),
    Kpop(
        "kpop",
        "wss://listen.moe/kpop/gateway_v2",
        "https://listen.moe/kpop/fallback"
    ),
    ;

    companion object {
        const val API_BASE = "https://listen.moe/graphql"
        const val API_SUBCRIPTIONS = "wss://listen.moe/subscriptions"

        const val CDN_ALBUM_ART_URL = "https://cdn.listen.moe/covers/"
        const val CDN_AVATAR_URL = "https://cdn.listen.moe/avatars/"
        const val CDN_BANNER_URL = "https://cdn.listen.moe/banners/"

        fun fromId(id: String): Library {
            return values().firstOrNull { it.id == id } ?: throw Exception("Unknown library type: $id")
        }
    }
}
