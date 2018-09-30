package me.echeung.moemoekyun.client.api.service

import me.echeung.moemoekyun.client.api.ErrorHandlingAdapter
import me.echeung.moemoekyun.client.api.response.SongsResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface SongsService {
    @GET("songs")
    fun getSongs(@Header("Authorization") token: String, @Header("library") library: String): ErrorHandlingAdapter.WrappedCall<SongsResponse>
}
