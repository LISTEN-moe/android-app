package me.echeung.moemoekyun.api.v4;

import me.echeung.moemoekyun.api.v4.services.AuthService;
import me.echeung.moemoekyun.api.v4.services.FavoritesService;
import me.echeung.moemoekyun.api.v4.services.SongsService;
import me.echeung.moemoekyun.api.v4.services.UsersService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {

    private static final String BASE_URL = "https://listen.moe/api/";

    private static final String HEADER_USER_AGENT = "User-Agent";
    private static final String USER_AGENT = "me.echeung.moemoekyun";

    private AuthService authService;
    private FavoritesService favoritesService;
    private SongsService songsService;
    private UsersService usersService;

    public APIClient() {
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(chain -> {
                    Request request = chain.request();

                    Request newRequest = request.newBuilder()
                            .addHeader(HEADER_USER_AGENT, USER_AGENT)
                            .build();

                    return chain.proceed(newRequest);
                })
                .build();

        final Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(new ErrorHandlingAdapter.ErrorHandlingCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authService = restAdapter.create(AuthService.class);
        favoritesService = restAdapter.create(FavoritesService.class);
        songsService = restAdapter.create(SongsService.class);
        usersService = restAdapter.create(UsersService.class);
    }

    public AuthService getAuthService() {
        return authService;
    }

    public FavoritesService getFavoritesService() {
        return favoritesService;
    }

    public SongsService getSongsService() {
        return songsService;
    }

    public UsersService getUsersService() {
        return usersService;
    }

    /*
    MyCall<Ip> ip = service.getIp();
    ip.enqueue(new MyCallback<Ip>() {
      @Override public void success(Response<Ip> response) {
        System.out.println("SUCCESS! " + response.body().origin);
      }

      @Override public void unauthenticated(Response<?> response) {
        System.out.println("UNAUTHENTICATED");
      }

      @Override public void clientError(Response<?> response) {
        System.out.println("CLIENT ERROR " + response.code() + " " + response.message());
      }

      @Override public void serverError(Response<?> response) {
        System.out.println("SERVER ERROR " + response.code() + " " + response.message());
      }

      @Override public void networkError(IOException e) {
        System.err.println("NETOWRK ERROR " + e.getMessage());
      }

      @Override public void unexpectedError(Throwable t) {
        System.err.println("FATAL ERROR " + t.getMessage());
      }
    });
     */
}
