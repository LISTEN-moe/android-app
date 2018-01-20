package me.echeung.listenmoeapi.services;

import me.echeung.listenmoeapi.ErrorHandlingAdapter;
import me.echeung.listenmoeapi.responses.BaseResponse;
import me.echeung.listenmoeapi.responses.SearchResponse;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface SongsService {
    @GET("songs")
    ErrorHandlingAdapter.WrappedCall<SearchResponse> songs(@Header("Authorization") String token);

    @GET("songs/{username}/uploads")
    ErrorHandlingAdapter.WrappedCall<BaseResponse> uploads(@Header("Authorization") String token, @Path("username") String username);
}
