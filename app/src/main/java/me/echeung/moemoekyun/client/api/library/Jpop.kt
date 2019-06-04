package me.echeung.moemoekyun.client.api.library

class Jpop private constructor() : Library(
        NAME,
        "wss://listen.moe/beta_gateway",
        "https://listen.moe/fallback") {
    companion object {
        const val NAME = "jpop"

        val INSTANCE: Library = Jpop()
    }
}
