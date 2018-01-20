package me.echeung.listenmoeapi.services;

import me.echeung.listenmoeapi.ErrorHandlingAdapter;
import me.echeung.listenmoeapi.responses.BaseResponse;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RequestsService {
    @POST("requests/{id}")
    ErrorHandlingAdapter.WrappedCall<BaseResponse> request(@Header("Authorization") String token, @Path("id") String id);
}
