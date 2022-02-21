package me.echeung.moemoekyun.client.api.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val uuid: String,
    val displayName: String,
    val avatarImage: String? = null,
    val bannerImage: String? = null,
)
