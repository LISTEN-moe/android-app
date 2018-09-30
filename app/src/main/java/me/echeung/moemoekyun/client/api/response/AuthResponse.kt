package me.echeung.moemoekyun.client.api.response

class AuthResponse : BaseResponse() {
    lateinit var token: String   // JWT token; only valid for ~2m if MFA is required
    var mfa: Boolean = false
}
