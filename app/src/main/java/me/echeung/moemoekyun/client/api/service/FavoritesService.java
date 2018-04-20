package me.echeung.moemoekyun.client.api.service;

import me.echeung.moemoekyun.client.api.ErrorHandlingAdapter;
import me.echeung.moemoekyun.client.api.response.BaseResponse;
import me.echeung.moemoekyun.client.api.response.FavoritesResponse;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FavoritesService {
    @POST("favorites/{id}")
    ErrorHandlingAdapter.WrappedCall<BaseResponse> favorite(@Header("Authorization") String token, @Path("id") String id);

    @DELETE("favorites/{id}")
    ErrorHandlingAdapter.WrappedCall<BaseResponse> removeFavorite(@Header("Authorization") String token, @Path("id") String id);

    @GET("favorites/{username}")
    ErrorHandlingAdapter.WrappedCall<FavoritesResponse> getFavorites(@Header("Authorization") String token, @Header("library") String library, @Path("username") String username);
}
