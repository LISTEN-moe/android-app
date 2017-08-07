package me.echeung.moemoekyun.ui;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import me.echeung.moemoekyun.service.StreamService;

public class App extends Application {

    private static StreamService mService;
    private static boolean mBound = false;

    @Override
    public void onCreate() {
        super.onCreate();

        Intent intent = new Intent(this, StreamService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public static StreamService getService() {
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
            StreamService.ServiceBinder binder = (StreamService.ServiceBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
