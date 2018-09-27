package me.echeung.moemoekyun.client.api.service

import me.echeung.moemoekyun.client.api.ErrorHandlingAdapter
import me.echeung.moemoekyun.client.api.response.AuthResponse
import me.echeung.moemoekyun.client.api.response.BaseResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthService {
    @POST("register")
    fun register(@Body body: RegisterBody): ErrorHandlingAdapter.WrappedCall<BaseResponse>

    @POST("login")
    fun login(@Body body: LoginBody): ErrorHandlingAdapter.WrappedCall<AuthResponse>

    @POST("login/mfa")
    fun mfa(@Header("Authorization") token: String, @Body body: LoginMfaBody): ErrorHandlingAdapter.WrappedCall<AuthResponse>

    class RegisterBody(val email: String, val username: String, val password: String)

    class LoginBody(val username: String, val password: String)

    class LoginMfaBody(val token: String)
}
