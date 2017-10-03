package me.echeung.moemoekyun;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import me.echeung.listenmoeapi.APIClient;
import me.echeung.moemoekyun.service.RadioService;
import me.echeung.moemoekyun.utils.AuthUtil;
import me.echeung.moemoekyun.viewmodels.RadioViewModel;
import me.echeung.moemoekyun.viewmodels.SearchViewModel;
import me.echeung.moemoekyun.viewmodels.UserViewModel;

public class App extends Application implements ServiceConnection {

    private static RadioService service;
    private static boolean isServiceBound = false;

    private static APIClient apiClient;

    private static RadioViewModel radioViewModel;
    private static SearchViewModel searchViewModel;
    private static UserViewModel userViewModel;

    @Override
    public void onCreate() {
        super.onCreate();

        // Music player service
        final Intent intent = new Intent(this, RadioService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);

        // API client
        apiClient = new APIClient(this, new APIClient.APIHelper() {
            @Override
            public boolean isAuthenticated() {
                return AuthUtil.isAuthenticated(getApplicationContext());
            }

            @Override
            public String getAuthToken() {
                return AuthUtil.getAuthToken(getApplicationContext());
            }
        });

        // UI view models
        radioViewModel = new RadioViewModel(this);
        searchViewModel = new SearchViewModel(this);
        userViewModel = new UserViewModel(this);
    }

    public static APIClient getApiClient() {
        return apiClient;
    }

    public static RadioViewModel getRadioViewModel() {
        return radioViewModel;
    }

    public static SearchViewModel getSearchViewModel() {
        return searchViewModel;
    }

    public static UserViewModel getUserViewModel() {
        return userViewModel;
    }

    public static RadioService getService() {
        return service;
    }

    public boolean isServiceBound() {
        return isServiceBound;
    }

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        final RadioService.ServiceBinder binder = (RadioService.ServiceBinder) service;
        final RadioService radioService = binder.getService();

        App.service = radioService;
        App.isServiceBound = true;
        App.apiClient.getSocket().setListener(radioService);
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        App.isServiceBound = false;
        App.apiClient.getSocket().setListener(null);
    }
}
