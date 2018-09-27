package me.echeung.moemoekyun.client.api.service

import me.echeung.moemoekyun.client.api.ErrorHandlingAdapter
import me.echeung.moemoekyun.client.api.response.SongsResponse
import me.echeung.moemoekyun.client.api.response.UploadsResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface SongsService {
    @GET("songs")
    fun getSongs(@Header("Authorization") token: String, @Header("library") library: String): ErrorHandlingAdapter.WrappedCall<SongsResponse>

    @GET("songs/{username}/uploads")
    fun getUploads(@Header("Authorization") token: String, @Header("library") library: String, @Path("username") username: String): ErrorHandlingAdapter.WrappedCall<UploadsResponse>
}
