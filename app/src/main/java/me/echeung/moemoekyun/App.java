package me.echeung.moemoekyun;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;

import me.echeung.moemoekyun.api.APIClient;
import me.echeung.moemoekyun.service.AppNotification;
import me.echeung.moemoekyun.service.RadioService;
import me.echeung.moemoekyun.utils.AuthUtil;
import me.echeung.moemoekyun.utils.PreferenceUtil;
import me.echeung.moemoekyun.viewmodels.AuthViewModel;
import me.echeung.moemoekyun.viewmodels.RadioViewModel;
import me.echeung.moemoekyun.viewmodels.UserViewModel;

public class App extends Application implements ServiceConnection {

    private static RadioService service;
    private static boolean isServiceBound = false;

    private static APIClient apiClient;

    private static AuthViewModel authViewModel;
    private static RadioViewModel radioViewModel;
    private static UserViewModel userViewModel;

    private static PreferenceUtil preferenceUtil;

    @Override
    public void onCreate() {
        super.onCreate();

        // Preferences
        preferenceUtil = new PreferenceUtil(this);

        // API client
        apiClient = new APIClient(this, getUserAgent(), preferenceUtil.getLibraryMode());

        // UI view models
        authViewModel = new AuthViewModel(this);
        radioViewModel = new RadioViewModel(this);
        userViewModel = new UserViewModel(this);

        // Music player service
        initNotificationChannel();
        initService();
    }

    public static APIClient getApiClient() {
        return apiClient;
    }

    public static AuthUtil getAuthUtil() {
        return apiClient.getAuthUtil();
    }

    public static AuthViewModel getAuthViewModel() {
        return authViewModel;
    }

    public static RadioViewModel getRadioViewModel() {
        return radioViewModel;
    }

    public static UserViewModel getUserViewModel() {
        return userViewModel;
    }

    public static PreferenceUtil getPreferenceUtil() {
        return preferenceUtil;
    }

    public static RadioService getService() {
        return service;
    }

    public static void setService(RadioService radioService) {
        if (!isServiceBound()) {
            service = radioService;
            isServiceBound = true;
        }
    }

    public static void clearService() {
        isServiceBound = false;
        apiClient.getSocket().setListener(null);
    }

    public static boolean isServiceBound() {
        return isServiceBound;
    }

    private void initService() {
        final Intent intent = new Intent(getApplicationContext(), RadioService.class);
        getApplicationContext().bindService(intent, this, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
    }

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        final RadioService.ServiceBinder binder = (RadioService.ServiceBinder) service;
        setService(binder.getService());
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        clearService();
    }

    private void initNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel notifChannel = new NotificationChannel(
                    AppNotification.NOTIFICATION_CHANNEL_ID,
                    AppNotification.NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW);

            final NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notifManager.createNotificationChannel(notifChannel);
        }
    }

    private String getUserAgent() {
        return String.format("%s/%s (%s; %s; Android %s)",
                BuildConfig.APPLICATION_ID,
                BuildConfig.VERSION_NAME,
                Build.DEVICE,
                Build.BRAND,
                Build.VERSION.SDK_INT);
    }

}
