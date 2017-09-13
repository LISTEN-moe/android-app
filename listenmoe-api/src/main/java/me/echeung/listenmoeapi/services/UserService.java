package me.echeung.listenmoeapi.services;

import me.echeung.listenmoeapi.ErrorHandlingAdapter;
import me.echeung.listenmoeapi.responses.UserFavoritesResponse;
import me.echeung.listenmoeapi.responses.UserResponse;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface UserService {
    @GET("user")
    ErrorHandlingAdapter.WrappedCall<UserResponse> getUserInfo(@Header("authorization") String token);

    @GET("user/favorites")
    ErrorHandlingAdapter.WrappedCall<UserFavoritesResponse> getFavorites(@Header("authorization") String token);
}
