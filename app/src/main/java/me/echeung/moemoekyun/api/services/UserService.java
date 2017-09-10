package me.echeung.moemoekyun.api.services;

import me.echeung.moemoekyun.api.models.UserFavorites;
import me.echeung.moemoekyun.api.models.UserInfo;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface UserService {
    @GET("user")
    Call<UserInfo> user(@Header("authorization") String token);

    @GET("user/favorites")
    Call<UserFavorites> favorites(@Header("authorization") String token);
}
