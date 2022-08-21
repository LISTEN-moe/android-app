package me.echeung.moemoekyun.client.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val uuid: String,
    val displayName: String,
    val avatarImage: String?,
    val bannerImage: String?,
)
