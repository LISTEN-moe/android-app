package me.echeung.moemoekyun.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaSessionCompat;

import java.util.Collections;
import java.util.List;

import me.echeung.moemoekyun.App;

public class AutoMediaBrowserService extends MediaBrowserServiceCompat implements ServiceConnection {

    private static final String MEDIA_ID_ROOT = "media_root";

    @Override
    public void onCreate() {
        super.onCreate();

        if (App.isServiceBound()) {
            setSessionToken();
        } else {
            final Intent intent = new Intent(getApplicationContext(), RadioService.class);
            getApplicationContext().bindService(intent, this, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
        }
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new MediaBrowserServiceCompat.BrowserRoot(MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(Collections.emptyList());
    }

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        final RadioService.ServiceBinder binder = (RadioService.ServiceBinder) service;
        final RadioService radioService = binder.getService();

        App.setService(radioService);
        setSessionToken();
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        App.clearService();
    }

    private void setSessionToken() {
        final MediaSessionCompat mediaSession = App.getService().getMediaSession();
        setSessionToken(mediaSession.getSessionToken());
    }
}
