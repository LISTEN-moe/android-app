package me.echeung.listenmoeapi.services;

import me.echeung.listenmoeapi.ErrorHandlingAdapter;
import me.echeung.listenmoeapi.responses.BaseResponse;
import me.echeung.listenmoeapi.responses.FavoriteResponse;
import me.echeung.listenmoeapi.responses.SearchResponse;
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
