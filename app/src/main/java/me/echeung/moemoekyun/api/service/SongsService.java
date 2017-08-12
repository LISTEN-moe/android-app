package me.echeung.moemoekyun.api.service;

import me.echeung.moemoekyun.api.model.response.SongsResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface SongsService {
    @GET("songs")
    Call<SongsResponse> songs(@Header("authorization") String token);
}
