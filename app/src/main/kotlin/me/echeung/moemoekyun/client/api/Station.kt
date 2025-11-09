package me.echeung.moemoekyun.client.api

import androidx.annotation.StringRes
import me.echeung.moemoekyun.R

enum class Station(val socketUrl: String, val streamUrl: String, @StringRes val labelRes: Int) {
    JPOP(
        "wss://listen.moe/gateway_v2",
        "https://listen.moe/fallback",
        R.string.jpop,
    ),
    KPOP(
        "wss://listen.moe/kpop/gateway_v2",
        "https://listen.moe/kpop/fallback",
        R.string.kpop,
    ),
}
