package me.echeung.moemoekyun.ui;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import me.echeung.moemoekyun.service.RadioService;

public class App extends Application {

    private static RadioService mService;
    private static boolean mBound = false;

    @Override
    public void onCreate() {
        super.onCreate();

        Intent intent = new Intent(this, RadioService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public static RadioService getService() {
        return mService;
    }

    public boolean isServiceBound() {
        return mBound;
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            RadioService.ServiceBinder binder = (RadioService.ServiceBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
