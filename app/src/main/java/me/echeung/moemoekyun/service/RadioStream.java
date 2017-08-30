package me.echeung.moemoekyun.service;

import android.net.Uri;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
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
import com.google.android.exoplayer2.util.Util;

import me.echeung.moemoekyun.constants.Endpoints;

public class RadioStream {

    private RadioService service;

    private SimpleExoPlayer player;

    public RadioStream(RadioService service) {
        this.service = service;
    }

    public void init() {
        final BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        final TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        final TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        final LoadControl loadControl = new DefaultLoadControl();
        final DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(service, Util.getUserAgent(service, service.getPackageName()));
        final ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        final RenderersFactory renderersFactory = new DefaultRenderersFactory(service);
        final MediaSource streamSource = new ExtractorMediaSource(Uri.parse(Endpoints.STREAM), dataSourceFactory, extractorsFactory, null, null);

        // TODO: simplify exoplayer init, possible memory leaks?
//        DataSource dataSource = new DefaultHttpDataSource(Util.getUserAgent(this, getPackageName()), null);
//        ExtractorMediaSource sampleSource = new ExtractorMediaSource(
//                Uri.parse(Endpoints.STREAM), dataSource, new Mp3Extractor(), 1, 5000);
//
//        MediaCodecAudioRenderer audioRenderer = new MediaCodecAudioRenderer(sampleSource);
//
//        player = ExoPlayerFactory.newInstance(audioRenderer);

        player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);
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

    public boolean isStarted() {
        return player != null;
    }

    public boolean isPlaying() {
        return player != null && player.getPlayWhenReady();
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
}
