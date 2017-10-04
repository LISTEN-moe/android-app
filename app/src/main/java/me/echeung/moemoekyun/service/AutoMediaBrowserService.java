package me.echeung.moemoekyun.service;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaSessionCompat;

import java.util.Collections;
import java.util.List;

import me.echeung.moemoekyun.App;

public class AutoMediaBrowserService extends MediaBrowserServiceCompat {

    private static final String MEDIA_ID_ROOT = "media_root";

    @Override
    public void onCreate() {
        super.onCreate();

        final MediaSessionCompat mediaSession = App.getService().getMediaSession();
        setSessionToken(mediaSession.getSessionToken());
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
}
