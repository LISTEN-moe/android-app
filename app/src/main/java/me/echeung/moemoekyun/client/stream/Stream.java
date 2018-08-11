package me.echeung.moemoekyun.client.stream;

import android.content.Context;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.echeung.moemoekyun.client.RadioClient;
import me.echeung.moemoekyun.util.system.NetworkUtil;

import static com.google.android.exoplayer2.C.CONTENT_TYPE_MUSIC;
import static com.google.android.exoplayer2.C.USAGE_MEDIA;

@RequiredArgsConstructor
public class Stream {

    private static final String WIFI_LOCK_TAG = "listenmoe_wifi_lock";

    private final Player.EventListener eventListener = new Player.DefaultEventListener() {
        @Override
        public void onPlayerError(ExoPlaybackException error) {
            // Try to reconnect to the stream
            boolean wasPlaying = isPlaying();

            releasePlayer();

            init(RadioClient.getLibrary().getStreamUrl());
            if (wasPlaying) {
                play();
            }
        }
    };

    private final Context context;

    private String currentStreamUrl;

    private WifiManager.WifiLock wifiLock;
    private SimpleExoPlayer player;

    @Setter
    private Listener listener;

    public void removeListener() {
        this.listener = null;
    }

    public boolean isStarted() {
        return player != null;
    }

    public boolean isPlaying() {
        return player != null && player.getPlayWhenReady();
    }

    public void play() {
        if (player == null || !currentStreamUrl.equals(RadioClient.getLibrary().getStreamUrl())) {
            init(RadioClient.getLibrary().getStreamUrl());
        }

        if (!isPlaying()) {
            acquireWifiLock();

            player.setPlayWhenReady(true);
            player.seekToDefaultPosition();
        }

        if (listener != null) {
            listener.onStreamPlay();
        }
    }

    public void pause() {
        if (player != null) {
            player.setPlayWhenReady(false);

            releaseWifiLock();
        }

        if (listener != null) {
            listener.onStreamPause();
        }
    }

    public void stop() {
        if (player != null) {
            player.stop(true);

            releasePlayer();
            releaseWifiLock();
        }

        if (listener != null) {
            listener.onStreamStop();
        }
    }

    public void fadeOut() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (player == null) {
                    stop();
                    if (listener != null) {
                        listener.onStreamStop();
                    }
                    return;
                }

                float vol = player.getVolume();
                float newVol = vol - 0.05f;
                if (newVol <= 0) {
                    stop();
                    if (listener != null) {
                        listener.onStreamStop();
                    }
                    return;
                }

                player.setVolume(newVol);

                handler.postDelayed(this, 200);
            }
        };

        handler.post(runnable);
    }

    public void duck() {
        if (player != null) {
            player.setVolume(0.5f);
        }
    }

    public void unduck() {
        if (player != null) {
            player.setVolume(1f);
        }
    }

    private void init(String streamUrl) {
        if (player == null) {
            player = ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector());

            player.addListener(eventListener);
            player.setVolume(1f);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(CONTENT_TYPE_MUSIC)
                    .setUsage(USAGE_MEDIA)
                    .build();
            player.setAudioAttributes(audioAttributes);
        }

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, NetworkUtil.getUserAgent());
        Uri streamUri = Uri.parse(streamUrl);
        MediaSource streamSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .setExtractorsFactory(new DefaultExtractorsFactory())
                .createMediaSource(streamUri);

        player.prepare(streamSource);

        currentStreamUrl = streamUrl;
    }

    private void releasePlayer() {
        if (player != null) {
            player.removeListener(eventListener);
            player.release();
            player = null;
        }
    }

    private void acquireWifiLock() {
        if (wifiLock == null) {
            this.wifiLock = ((WifiManager) context.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE))
                    .createWifiLock(WifiManager.WIFI_MODE_FULL, WIFI_LOCK_TAG);
        }

        if (!wifiLock.isHeld()) {
            wifiLock.acquire();
        }
    }

    private void releaseWifiLock() {
        if (wifiLock != null && wifiLock.isHeld()) {
            wifiLock.release();
        }
    }

    public interface Listener {
        void onStreamPlay();
        void onStreamPause();
        void onStreamStop();
    }

}
