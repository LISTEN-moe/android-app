package me.echeung.moemoekyun.client.api.service

import me.echeung.moemoekyun.client.api.ErrorHandlingAdapter
import me.echeung.moemoekyun.client.api.response.UserResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface UsersService {
    @GET("users/{username}")
    fun getUserInfo(@Header("Authorization") token: String, @Path("username") username: String): ErrorHandlingAdapter.WrappedCall<UserResponse>
}
