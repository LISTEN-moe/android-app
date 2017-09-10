package me.echeung.moemoekyun.api.services;

import me.echeung.moemoekyun.api.models.UserFavorites;
import me.echeung.moemoekyun.api.responses.FavoriteResponse;
import me.echeung.moemoekyun.api.responses.RequestResponse;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SongsService {
    @POST("songs/favourite")
    Call<FavoriteResponse> favorite(@Header("authorization") String token, @Query("song") int songId);

    @POST("songs/request")
    Call<RequestResponse> request(@Header("authorization") String token, @Query("song") int songId);

    @POST("songs/search")
    Call<UserFavorites> search(@Header("authorization") String token, @Query("query") String query);
}
