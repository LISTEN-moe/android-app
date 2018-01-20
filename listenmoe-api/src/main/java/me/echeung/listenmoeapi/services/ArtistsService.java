package me.echeung.listenmoeapi.services;

import me.echeung.listenmoeapi.ErrorHandlingAdapter;
import me.echeung.listenmoeapi.responses.BaseResponse;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ArtistsService {
    @GET("artists")
    ErrorHandlingAdapter.WrappedCall<BaseResponse> artists(@Header("Authorization") String token);

    @POST("artists/{id}")
    ErrorHandlingAdapter.WrappedCall<BaseResponse> artist(@Header("Authorization") String token, @Path("id") String id);
}
