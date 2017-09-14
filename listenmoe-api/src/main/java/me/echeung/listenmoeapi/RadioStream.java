package me.echeung.listenmoeapi;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

public class RadioStream {

    // Vorbis: /stream, Opus: /opus, mp3: /fallback
    private static final String STREAM_URL = "https://listen.moe/fallback";

    private Context context;
    private SimpleExoPlayer player;

    RadioStream(Context context) {
        this.context = context;
    }

    public boolean isPlaying() {
        return player != null && player.getPlayWhenReady();
    }

    public boolean isStarted() {
        return player != null;
    }

    public void setVolume(float volume) {
        if (player != null) {
            player.setVolume(volume);
        }
    }

    public void play() {
        if (player == null) {
            init();
        }

        player.setPlayWhenReady(true);
        player.seekToDefaultPosition();
    }

    public void pause() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    public void stop() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player = null;
        }
    }

    private void init() {
        final BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        final TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        final TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        final DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, APIClient.USER_AGENT);
        final ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        final MediaSource streamSource = new ExtractorMediaSource(Uri.parse(STREAM_URL), dataSourceFactory, extractorsFactory, null, null);

        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        player.prepare(streamSource);

        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                // Try to reconnect to the stream
                final boolean wasPlaying = isPlaying();

                if (player != null) {
                    player.release();
                    player = null;
                }

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
        });
    }
}
