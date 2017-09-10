package me.echeung.moemoekyun;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import me.echeung.moemoekyun.api.APIClient;
import me.echeung.moemoekyun.service.RadioService;
import me.echeung.moemoekyun.viewmodels.RadioViewModel;
import me.echeung.moemoekyun.viewmodels.SearchViewModel;
import me.echeung.moemoekyun.viewmodels.UserViewModel;

public class App extends Application {

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
        Intent intent = new Intent(this, RadioService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT );

        // API client
        apiClient = new APIClient();

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

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            RadioService.ServiceBinder binder = (RadioService.ServiceBinder) service;
            App.service = binder.getService();
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isServiceBound = false;
        }
    };
}
