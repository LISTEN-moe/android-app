package me.echeung.moemoekyun.client.api.library

class Kpop private constructor() : Library(
        NAME,
        // TODO: update with new socket URL whenever that's available
        "wss://listen.moe/kpop/gateway",
        "https://listen.moe/kpop/fallback") {
    companion object {
        const val NAME = "kpop"

        val INSTANCE: Library = Kpop()
    }
}
