package me.echeung.moemoekyun.client.model

import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val name: String,
    val image: String?,
)
