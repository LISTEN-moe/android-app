package me.echeung.listenmoeapi.services;

import me.echeung.listenmoeapi.ErrorHandlingAdapter;
import me.echeung.listenmoeapi.responses.BaseResponse;
import me.echeung.listenmoeapi.responses.SearchResponse;
import me.echeung.listenmoeapi.responses.SongsResponse;
import me.echeung.listenmoeapi.responses.UploadsResponse;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface SongsService {
    @GET("songs")
    ErrorHandlingAdapter.WrappedCall<SongsResponse> songs(@Header("Authorization") String token);

    @GET("songs/{username}/uploads")
    ErrorHandlingAdapter.WrappedCall<UploadsResponse> uploads(@Header("Authorization") String token, @Path("username") String username);
}
