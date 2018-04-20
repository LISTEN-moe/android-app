package me.echeung.moemoekyun.client.api.service;

import me.echeung.moemoekyun.client.api.ErrorHandlingAdapter;
import me.echeung.moemoekyun.client.api.response.SongsResponse;
import me.echeung.moemoekyun.client.api.response.UploadsResponse;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface SongsService {
    @GET("songs")
    ErrorHandlingAdapter.WrappedCall<SongsResponse> getSongs(@Header("Authorization") String token, @Header("library") String library);

    @GET("songs/{username}/uploads")
    ErrorHandlingAdapter.WrappedCall<UploadsResponse> getUploads(@Header("Authorization") String token, @Header("library") String library, @Path("username") String username);
}
