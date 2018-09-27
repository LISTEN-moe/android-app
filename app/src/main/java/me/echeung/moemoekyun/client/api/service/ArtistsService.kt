package me.echeung.moemoekyun.client.api.service

import me.echeung.moemoekyun.client.api.ErrorHandlingAdapter
import me.echeung.moemoekyun.client.api.response.ArtistResponse
import me.echeung.moemoekyun.client.api.response.ArtistsResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ArtistsService {
    @GET("artists")
    fun getArtists(@Header("Authorization") token: String, @Header("library") library: String): ErrorHandlingAdapter.WrappedCall<ArtistsResponse>

    @POST("artists/{id}")
    fun getArtist(@Header("Authorization") token: String, @Header("library") library: String, @Path("id") id: String): ErrorHandlingAdapter.WrappedCall<ArtistResponse>
}
