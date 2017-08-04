package me.echeung.moemoekyun.ui;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.BaseObservable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.IBinder;

import me.echeung.moemoekyun.model.Song;
import me.echeung.moemoekyun.service.StreamService;

public class App extends Application {

    // Data binding state
    public static final AppState STATE = new AppState();

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

    public static class AppState extends BaseObservable {
        // Network state
        public final ObservableBoolean hasNetworkConnection = new ObservableBoolean();

        // Play state
        public final ObservableBoolean playing = new ObservableBoolean();
        public final ObservableField<Song> currentSong = new ObservableField<>();
        public final ObservableBoolean currentFavorited = new ObservableBoolean();
        public final ObservableInt listeners = new ObservableInt();
        public final ObservableField<String> requester = new ObservableField<>();
    }

    public boolean isServiceBound() {
        return mBound;
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            StreamService.LocalBinder binder = (StreamService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
