package me.echeung.moemoekyun.api.v4.services;

import me.echeung.moemoekyun.api.v4.models.NewUser;
import me.echeung.moemoekyun.api.v4.responses.AuthResponse;
import me.echeung.moemoekyun.api.v4.responses.BasicResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AuthService {
    @POST("auth/login")
    Call<AuthResponse> login(@Query("username") String username, @Query("password") String password);

    @POST("auth/register")
    Call<BasicResponse> register(@Body NewUser newUser);

    @POST("auth/verify/{key}")
    Call<BasicResponse> verify(@Path("key") String key);
}
