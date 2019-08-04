package me.echeung.moemoekyun.client.api.library

class Kpop private constructor() : Library(
        NAME,
        "wss://listen.moe/kpop/gateway_v2",
        "https://listen.moe/kpop/fallback") {
    companion object {
        const val NAME = "kpop"

        val INSTANCE: Library = Kpop()
    }
}
