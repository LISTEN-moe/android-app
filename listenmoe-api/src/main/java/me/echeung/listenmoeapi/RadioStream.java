package me.echeung.listenmoeapi;

import android.content.Context;
import android.net.Uri;
import android.net.wifi.WifiManager;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import static com.google.android.exoplayer2.C.CONTENT_TYPE_MUSIC;
import static com.google.android.exoplayer2.C.USAGE_MEDIA;

public class RadioStream {

    // Vorbis: /stream, Opus: /opus, mp3: /fallback
    private static final String STREAM_URL = "https://listen.moe/fallback";

    private static final String WIFI_LOCK_TAG = "listenmoe_wifi_lock";

    private final Context context;
    private final Player.EventListener eventListener;
    private final WifiManager.WifiLock wifiLock;

    private SimpleExoPlayer player;

    RadioStream(Context context) {
        this.context = context;

        this.wifiLock =
                ((WifiManager) context.getApplicationContext()
                        .getSystemService(Context.WIFI_SERVICE))
                        .createWifiLock(WifiManager.WIFI_MODE_FULL, WIFI_LOCK_TAG);

        this.eventListener = new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                // Try to reconnect to the stream
                final boolean wasPlaying = isPlaying();

                releasePlayer();

                init();
                if (wasPlaying) {
                    play();
                }
            }

            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
            }

            @Override
            public void onPositionDiscontinuity() {
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            }
        };
    }

    public boolean isPlaying() {
        return player != null && player.getPlayWhenReady();
    }

    public boolean isStarted() {
        return player != null;
    }

    public void play() {
        if (player == null) {
            init();
        }

        if (wifiLock != null) {
            wifiLock.acquire();
        }

        player.setPlayWhenReady(true);
        player.seekToDefaultPosition();
    }

    public void pause() {
        if (player != null) {
            player.setPlayWhenReady(false);

            releaseWifiLock();
        }
    }

    public void stop() {
        if (player != null) {
            player.setPlayWhenReady(false);

            releaseWifiLock();
            releasePlayer();
        }
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

    private void init() {
        final DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, APIClient.USER_AGENT);
        final ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        final MediaSource streamSource = new ExtractorMediaSource(Uri.parse(STREAM_URL), dataSourceFactory, extractorsFactory, null, null);

        player = ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector());
        player.prepare(streamSource);
        player.addListener(eventListener);

        final AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(CONTENT_TYPE_MUSIC)
                .setUsage(USAGE_MEDIA)
                .build();
        player.setAudioAttributes(audioAttributes);
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player.removeListener(eventListener);
            player = null;
        }
    }

    private void releaseWifiLock() {
        if (wifiLock != null && wifiLock.isHeld()) {
            wifiLock.release();
        }
    }
}
