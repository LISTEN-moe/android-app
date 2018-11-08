package me.echeung.moemoekyun.client.api.v5.library

class Kpop private constructor() : Library(
        NAME,
        "wss://listen.moe/kpop/gateway",
        "https://listen.moe/kpop/fallback") {
    companion object {
        const val NAME = "kpop"

        val INSTANCE: Library = Kpop()
    }
}
