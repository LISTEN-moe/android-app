package me.echeung.moemoekyun.client.api.v4.service

import me.echeung.moemoekyun.client.api.v4.ErrorHandlingAdapter
import me.echeung.moemoekyun.client.api.v4.response.UserResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface UsersService {
    @GET("users/{username}")
    fun getUserInfo(@Header("Authorization") token: String, @Path("username") username: String): ErrorHandlingAdapter.WrappedCall<UserResponse>
}
