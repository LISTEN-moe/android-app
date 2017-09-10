package me.echeung.moemoekyun.api.services;

import me.echeung.moemoekyun.api.ErrorHandlingAdapter;
import me.echeung.moemoekyun.api.responses.UserFavoritesResponse;
import me.echeung.moemoekyun.api.responses.UserResponse;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface UserService {
    @GET("user")
    ErrorHandlingAdapter.WrappedCall<UserResponse> getUserInfo(@Header("authorization") String token);

    @GET("user/favorites")
    ErrorHandlingAdapter.WrappedCall<UserFavoritesResponse> getFavorites(@Header("authorization") String token);
}
