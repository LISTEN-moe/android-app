package me.echeung.moemoekyun.client.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val name: String,
    val image: String? = null,
)
