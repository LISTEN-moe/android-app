package me.echeung.moemoekyun.service;

import android.app.UiModeManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;
import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;

public class AutoMediaBrowserService extends MediaBrowserServiceCompat implements ServiceConnection {

    private static final String MEDIA_ID_ROOT = "media_root";

    @Override
    public void onCreate() {
        super.onCreate();

        if (App.Companion.isServiceBound()) {
            setSessionToken();
        } else {
            Intent intent = new Intent(getApplicationContext(), RadioService.class);
            getApplicationContext().bindService(intent, this, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
        }
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new MediaBrowserServiceCompat.BrowserRoot(MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        mediaItems.add(createPlayableMediaItem(RadioService.LIBRARY_JPOP, getResources().getString(R.string.jpop)));
        mediaItems.add(createPlayableMediaItem(RadioService.LIBRARY_KPOP, getResources().getString(R.string.kpop)));

        result.sendResult(mediaItems);
    }

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        RadioService.ServiceBinder binder = (RadioService.ServiceBinder) service;
        RadioService radioService = binder.getService();

        App.Companion.setService(radioService);
        setSessionToken();
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        App.Companion.clearService();
    }

    private void setSessionToken() {
        MediaSessionCompat mediaSession = App.Companion.getService().getMediaSession();
        setSessionToken(mediaSession.getSessionToken());
    }

    private MediaBrowserCompat.MediaItem createPlayableMediaItem(String mediaId, String title) {
        MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder()
                .setMediaId(mediaId)
                .setTitle(title);

        return new MediaBrowserCompat.MediaItem(builder.build(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
    }

    public static boolean isCarUiMode(Context context) {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        return uiModeManager != null && uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_CAR;
    }

}
