package me.echeung.moemoekyun.api.services;

import me.echeung.moemoekyun.api.ErrorHandlingAdapter;
import me.echeung.moemoekyun.api.responses.AuthResponse;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface AuthService {
    @FormUrlEncoded
    @POST("authenticate")
    ErrorHandlingAdapter.WrappedCall<AuthResponse> login(@Field("username") String username, @Field("password") String password);
}
