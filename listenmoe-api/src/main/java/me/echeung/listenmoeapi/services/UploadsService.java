package me.echeung.listenmoeapi.services;

import me.echeung.listenmoeapi.ErrorHandlingAdapter;
import me.echeung.listenmoeapi.responses.BaseResponse;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface UploadsService {
    @GET("uploads/{username}")
    ErrorHandlingAdapter.WrappedCall<BaseResponse> uploads(@Header("Authorization") String token, @Path("username") String username);
}
