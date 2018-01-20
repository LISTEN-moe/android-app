package me.echeung.listenmoeapi.services;

import me.echeung.listenmoeapi.ErrorHandlingAdapter;
import me.echeung.listenmoeapi.responses.BaseResponse;
import me.echeung.listenmoeapi.responses.FavoriteResponse;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FavoritesService {
    @POST("favorites/{id}")
    ErrorHandlingAdapter.WrappedCall<FavoriteResponse> favorite(@Header("Authorization") String token, @Path("id") String id);

    @DELETE("favorites/{id}")
    ErrorHandlingAdapter.WrappedCall<BaseResponse> removeFavorite(@Header("Authorization") String token, @Path("id") String id);

    @GET("favorites/{username}")
    ErrorHandlingAdapter.WrappedCall<BaseResponse> favorites(@Header("Authorization") String token, @Path("username") String username);
}
