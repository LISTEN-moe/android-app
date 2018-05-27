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

import me.echeung.moemoekyun.client.RadioClient;
import me.echeung.moemoekyun.service.AppNotification;
import me.echeung.moemoekyun.service.RadioService;
import me.echeung.moemoekyun.util.AuthUtil;
import me.echeung.moemoekyun.util.PreferenceUtil;
import me.echeung.moemoekyun.viewmodel.AuthViewModel;
import me.echeung.moemoekyun.viewmodel.RadioViewModel;
import me.echeung.moemoekyun.viewmodel.UserViewModel;

public class App extends Application implements ServiceConnection {

    private static RadioService service;
    private static boolean isServiceBound = false;

    private static RadioClient radioClient;

    private static AuthViewModel authViewModel;
    private static RadioViewModel radioViewModel;
    private static UserViewModel userViewModel;

    private static PreferenceUtil preferenceUtil;

    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        preferenceUtil = new PreferenceUtil(this);

        radioClient = new RadioClient(this);

        // UI view models
        authViewModel = new AuthViewModel();
        radioViewModel = new RadioViewModel();
        userViewModel = new UserViewModel();

        // Music player service
        initNotificationChannel();
        initService();
    }

    public static Context getContext() {
        return instance;
    }

    public static RadioClient getRadioClient() {
        return radioClient;
    }

    public static AuthUtil getAuthUtil() {
        return radioClient.getAuthUtil();
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
        radioClient.getSocket().setListener(null);
    }

    public static boolean isServiceBound() {
        return isServiceBound;
    }

    private void initService() {
        Intent intent = new Intent(getApplicationContext(), RadioService.class);
        getApplicationContext().bindService(intent, this, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
    }

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        RadioService.ServiceBinder binder = (RadioService.ServiceBinder) service;
        setService(binder.getService());
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        clearService();
    }

    private void initNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notifChannel = new NotificationChannel(
                    AppNotification.NOTIFICATION_CHANNEL_ID,
                    AppNotification.NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW);

            NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notifManager.createNotificationChannel(notifChannel);
        }
    }

}
