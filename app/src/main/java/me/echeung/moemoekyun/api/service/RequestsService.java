package me.echeung.moemoekyun.api.service;

import me.echeung.moemoekyun.api.ErrorHandlingAdapter;
import me.echeung.moemoekyun.api.response.BaseResponse;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RequestsService {
    @POST("requests/{id}")
    ErrorHandlingAdapter.WrappedCall<BaseResponse> request(@Header("Authorization") String token, @Path("id") String id);
}
