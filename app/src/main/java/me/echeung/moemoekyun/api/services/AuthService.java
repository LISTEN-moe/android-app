package me.echeung.moemoekyun.api.services;

import me.echeung.moemoekyun.api.responses.AuthResponse;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AuthService {
    @POST("authenticate")
    Call<AuthResponse> authenticate(@Query("username") String username, @Query("password") String password);
}
