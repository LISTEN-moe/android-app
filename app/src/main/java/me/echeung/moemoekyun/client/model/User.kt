package me.echeung.moemoekyun.client.model

class User {
    var uuid: String? = null
    var email: String? = null
    var username: String? = null
    var displayName: String? = null
    var avatarImage: String? = null
    var bannerImage: String? = null
    var bio: String? = null
    var additionalRequests: Int = 0
    var uploads: Int = 0
    var uploadLimit: Int = 0
    var favorites: Int = 0
    var requestsRemaining: Int = 0
}
