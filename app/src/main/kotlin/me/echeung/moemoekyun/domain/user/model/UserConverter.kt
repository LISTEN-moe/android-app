package me.echeung.moemoekyun.domain.user.model

import me.echeung.moemoekyun.client.model.User
import javax.inject.Inject

class UserConverter @Inject constructor() {

    fun toDomainUser(user: User): DomainUser {
        return DomainUser(
            username = user.displayName,
            avatarUrl = "$CDN_AVATAR_URL/${user.avatarImage}".takeIf { user.avatarImage != null },
            bannerUrl = "$CDN_BANNER_URL/${user.bannerImage}".takeIf { user.bannerImage != null },
        )
    }
}

private const val CDN_AVATAR_URL = "https://cdn.listen.moe/avatars/"
private const val CDN_BANNER_URL = "https://cdn.listen.moe/banners/"
