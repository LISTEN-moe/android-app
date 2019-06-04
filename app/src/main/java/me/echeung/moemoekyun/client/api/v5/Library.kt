package me.echeung.moemoekyun.client.api.v5

abstract class Library(val name: String, val socketUrl: String, val streamUrl: String) {
    companion object {
        const val API_BASE = "https://listen.moe/graphql"
    }
}
