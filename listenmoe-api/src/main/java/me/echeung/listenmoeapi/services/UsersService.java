package me.echeung.listenmoeapi.services;

import me.echeung.listenmoeapi.ErrorHandlingAdapter;
import me.echeung.listenmoeapi.responses.UserResponse;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface UsersService {
    @GET("users/{username}")
    ErrorHandlingAdapter.WrappedCall<UserResponse> getUserInfo(@Header("authorization") String token, @Path("username") String username);
}
