package me.echeung.listenmoeapi.services;

import lombok.AllArgsConstructor;
import me.echeung.listenmoeapi.ErrorHandlingAdapter;
import me.echeung.listenmoeapi.responses.AuthResponse;
import me.echeung.listenmoeapi.responses.BaseResponse;
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
    public class RegisterBody {
        final String email;
        final String username;
        final String password;
    }

    @AllArgsConstructor
    public class LoginBody {
        final String username;
        final String password;
    }

    @AllArgsConstructor
    public class LoginMfaBody {
        final String token;
    }
}
