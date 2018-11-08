package me.echeung.moemoekyun.client.api.v4.library

class Jpop private constructor() : Library(
        NAME,
        "wss://listen.moe/gateway",
        "https://listen.moe/fallback") {
    companion object {
        const val NAME = "jpop"

        val INSTANCE: Library = Jpop()
    }
}
