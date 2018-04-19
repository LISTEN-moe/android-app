package me.echeung.moemoekyun.api.services;

import me.echeung.moemoekyun.api.ErrorHandlingAdapter;
import me.echeung.moemoekyun.api.responses.ArtistResponse;
import me.echeung.moemoekyun.api.responses.ArtistsResponse;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ArtistsService {
    @GET("artists")
    ErrorHandlingAdapter.WrappedCall<ArtistsResponse> getArtists(@Header("Authorization") String token, @Header("library") String library);

    @POST("artists/{id}")
    ErrorHandlingAdapter.WrappedCall<ArtistResponse> getArtist(@Header("Authorization") String token, @Header("library") String library, @Path("id") String id);
}
