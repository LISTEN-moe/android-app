package me.echeung.moemoekyun.api.services;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

import me.echeung.moemoekyun.api.responses.UserResponse;

public interface UsersService {
    @GET("users/@me")
    Call<UserResponse> user(@Header("authorization") String token);

    @GET("users/{username}")
    Call<UserResponse> user(@Header("authorization") String token, @Path("username") String username);
}
