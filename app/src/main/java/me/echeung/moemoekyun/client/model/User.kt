package me.echeung.moemoekyun.client.model

data class User(
    val uuid: String,
    val displayName: String,
    val avatarImage: String?,
    val bannerImage: String?
)
