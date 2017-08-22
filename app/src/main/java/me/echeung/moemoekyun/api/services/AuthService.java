package me.echeung.moemoekyun.api.services;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import me.echeung.moemoekyun.api.models.NewUser;
import me.echeung.moemoekyun.api.responses.AuthResponse;
import me.echeung.moemoekyun.api.responses.BasicResponse;

public interface AuthService {
    @POST("auth/login")
    Call<AuthResponse> login(@Query("username") String username, @Query("password") String password);

    @POST("auth/register")
    Call<BasicResponse> register(@Body NewUser newUser);

    @POST("auth/verify/{key}")
    Call<BasicResponse> verify(@Path("key") String key);
}
