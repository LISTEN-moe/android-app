package me.echeung.moemoekyun.client.api.v4.service

import me.echeung.moemoekyun.client.api.v4.ErrorHandlingAdapter
import me.echeung.moemoekyun.client.api.v4.response.BaseResponse
import me.echeung.moemoekyun.client.api.v4.response.FavoritesResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface FavoritesService {
    @POST("favorites/{id}")
    fun favorite(@Header("Authorization") token: String, @Path("id") id: String): ErrorHandlingAdapter.WrappedCall<BaseResponse>

    @DELETE("favorites/{id}")
    fun removeFavorite(@Header("Authorization") token: String, @Path("id") id: String): ErrorHandlingAdapter.WrappedCall<BaseResponse>

    @GET("favorites/{username}")
    fun getFavorites(@Header("Authorization") token: String, @Header("library") library: String, @Path("username") username: String): ErrorHandlingAdapter.WrappedCall<FavoritesResponse>
}
