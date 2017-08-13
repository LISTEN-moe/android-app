package me.echeung.moemoekyun.api;

import me.echeung.moemoekyun.api.service.AuthService;
import me.echeung.moemoekyun.api.service.FavoritesService;
import me.echeung.moemoekyun.api.service.SongsService;
import me.echeung.moemoekyun.api.service.UsersService;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {

    private static final String BASE_URL = "https://listen.moe/api/";

    private AuthService authService;
    private FavoritesService favoritesService;
    private SongsService songsService;
    private UsersService usersService;

    public APIClient() {
        final Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(BASE_URL)
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
}
