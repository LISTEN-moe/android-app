package me.echeung.moemoekyun.client.api.response

class AuthResponse : BaseResponse() {
    val token: String? = null   // JWT token; only valid for ~2m if MFA is required
    val apiKey: String? = null  // Only if requested via developer access
    val isMfa: Boolean = false
}
