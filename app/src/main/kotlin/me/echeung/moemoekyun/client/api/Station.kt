package me.echeung.moemoekyun.client.api

import androidx.annotation.StringRes
import me.echeung.moemoekyun.R

enum class Station(
    val socketUrl: String,
    val streamUrl: String,
    val fallbackStreamUrl: String,
    val sseMounts: List<String>,
    val sseService: String,
    @StringRes val labelRes: Int,
) {
    JPOP(
        "wss://listen.moe/gateway_v2",
        "https://listen.moe/stream",
        "https://listen.moe/fallback",
        listOf("/stream", "/fallback"),
        "jpop",
        R.string.jpop,
    ),
    KPOP(
        "wss://listen.moe/kpop/gateway_v2",
        "https://listen.moe/kpop/stream",
        "https://listen.moe/kpop/fallback",
        listOf("/kpop/stream", "/kpop/fallback"),
        "kpop",
        R.string.kpop,
    ),
}
