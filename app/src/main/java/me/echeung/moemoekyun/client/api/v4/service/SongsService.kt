package me.echeung.moemoekyun.client.api.v4.service

import me.echeung.moemoekyun.client.api.v4.ErrorHandlingAdapter
import me.echeung.moemoekyun.client.api.v4.response.SongsResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface SongsService {
    @GET("songs")
    fun getSongs(@Header("Authorization") token: String, @Header("library") library: String): ErrorHandlingAdapter.WrappedCall<SongsResponse>
}
