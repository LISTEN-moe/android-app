package me.echeung.moemoekyun.client.api.library

abstract class Library(val name: String, val socketUrl: String, val streamUrl: String) {
    companion object {
        const val API_BASE = "https://listen.moe/api/"

        const val CDN_ALBUM_ART_URL = "https://cdn.listen.moe/covers/"
        const val CDN_AVATAR_URL = "https://cdn.listen.moe/avatars/"
        const val CDN_BANNER_URL = "https://cdn.listen.moe/banners/"
    }
}
