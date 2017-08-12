package me.echeung.moemoekyun.api.service;

import me.echeung.moemoekyun.api.model.response.BasicResponse;
import me.echeung.moemoekyun.api.model.response.SongsResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FavoritesService {
    @GET("favorites/@me")
    Call<SongsResponse> favorites(@Header("authorization") String token);

    @GET("favorites/{username}")
    Call<SongsResponse> favorites(@Header("authorization") String token, @Path("username") String username);

    @POST("favourites/add/{songId}")
    Call<BasicResponse> add(@Header("authorization") String token, @Path("songId") int songId);

    @POST("favourites/remove/{songId}")
    Call<BasicResponse> remove(@Header("authorization") String token, @Path("songId") int songId);
}
