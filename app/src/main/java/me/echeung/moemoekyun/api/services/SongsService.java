package me.echeung.moemoekyun.api.services;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

import me.echeung.moemoekyun.api.responses.SongsResponse;

public interface SongsService {
    @GET("songs")
    Call<SongsResponse> songs(@Header("authorization") String token);
}
