package me.echeung.moemoekyun.api.services;

import me.echeung.moemoekyun.api.ErrorHandlingAdapter;
import me.echeung.moemoekyun.api.responses.UserResponse;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface UsersService {
    @GET("users/{username}")
    ErrorHandlingAdapter.WrappedCall<UserResponse> getUserInfo(@Header("Authorization") String token, @Path("username") String username);
}
