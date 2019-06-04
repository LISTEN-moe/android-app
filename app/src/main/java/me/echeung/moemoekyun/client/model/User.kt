package me.echeung.moemoekyun.client.model

data class User(
        val displayName: String,
        val avatarImage: String?,
        val bannerImage: String?,

        // Mutable field, to handle quickly updating in UI
        var requestsRemaining: Int
)
