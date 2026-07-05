package me.echeung.moemoekyun.client.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ListenMoeApi {

    @POST("api/v1/search")
    suspend fun search(@Body body: SearchRequest): SearchResponse

    @GET("api/v1/charts/songs")
    suspend fun chartSongs(
        @Query("station") station: String,
        @Query("window") window: String,
        @Query("mode") mode: String,
    ): ChartSongsResponse

    @GET("api/v1/charts/artists")
    suspend fun chartArtists(
        @Query("station") station: String,
        @Query("window") window: String,
        @Query("mode") mode: String,
    ): ChartEntitiesResponse

    @GET("api/v1/charts/albums")
    suspend fun chartAlbums(
        @Query("station") station: String,
        @Query("window") window: String,
        @Query("mode") mode: String,
    ): ChartEntitiesResponse
}
