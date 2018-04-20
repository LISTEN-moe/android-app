package me.echeung.moemoekyun.api.service;

import lombok.AllArgsConstructor;
import me.echeung.moemoekyun.api.ErrorHandlingAdapter;
import me.echeung.moemoekyun.api.response.AuthResponse;
import me.echeung.moemoekyun.api.response.BaseResponse;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AuthService {
    @POST("register")
    ErrorHandlingAdapter.WrappedCall<BaseResponse> register(@Body RegisterBody body);

    @POST("login")
    ErrorHandlingAdapter.WrappedCall<AuthResponse> login(@Body LoginBody body);

    @POST("login/mfa")
    ErrorHandlingAdapter.WrappedCall<AuthResponse> mfa(@Header("Authorization") String token, @Body LoginMfaBody body);

    @AllArgsConstructor
    class RegisterBody {
        String email;
        String username;
        String password;
    }

    @AllArgsConstructor
    class LoginBody {
        String username;
        String password;
    }

    @AllArgsConstructor
    class LoginMfaBody {
        String token;
    }
}
