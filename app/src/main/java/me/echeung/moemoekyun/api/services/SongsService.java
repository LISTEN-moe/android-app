package me.echeung.moemoekyun.api.services;

import me.echeung.moemoekyun.api.ErrorHandlingAdapter;
import me.echeung.moemoekyun.api.responses.BaseResponse;
import me.echeung.moemoekyun.api.responses.FavoriteResponse;
import me.echeung.moemoekyun.api.responses.SearchResponse;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface SongsService {
    @FormUrlEncoded
    @POST("songs/favorite")
    ErrorHandlingAdapter.WrappedCall<FavoriteResponse> favorite(@Header("authorization") String token, @Field("song") int songId);

    @FormUrlEncoded
    @POST("songs/request")
    ErrorHandlingAdapter.WrappedCall<BaseResponse> request(@Header("authorization") String token, @Field("song") int songId);

    @FormUrlEncoded
    @POST("songs/search")
    ErrorHandlingAdapter.WrappedCall<SearchResponse> search(@Header("authorization") String token, @Field("query") String query);
}
