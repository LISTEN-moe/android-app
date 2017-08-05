package me.echeung.moemoekyun.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
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
import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.IOException;

import me.echeung.moemoekyun.constants.Endpoints;
import me.echeung.moemoekyun.constants.ResponseMessages;
import me.echeung.moemoekyun.interfaces.FavoriteSongListener;
import me.echeung.moemoekyun.model.PlaybackInfo;
import me.echeung.moemoekyun.model.Song;
import me.echeung.moemoekyun.service.notification.AppNotification;
import me.echeung.moemoekyun.ui.App;
import me.echeung.moemoekyun.ui.activities.MainActivity;
import me.echeung.moemoekyun.util.APIUtil;
import me.echeung.moemoekyun.util.AuthUtil;
import me.echeung.moemoekyun.util.NetworkUtil;

public class StreamService extends Service {

    public static final String PLAY_PAUSE = "play_pause";
    public static final String STOP = "stop";
    public static final String TOGGLE_FAVORITE = "toggle_favorite";

    private static final int SOCKET_TIMEOUT = 900000;
    private static final int RETRY_TIME = 1500;

    private SimpleExoPlayer player;
    private WebSocket socket;

    private AppNotification notification;

    private final IBinder mBinder = new LocalBinder();
    private boolean isServiceBound = false;
    public class LocalBinder extends Binder {
        public StreamService getService() {
            return StreamService.this;
        }
    }

    private static final Gson GSON = new Gson();

    private BroadcastReceiver intentReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        isServiceBound = true;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        isServiceBound = true;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isServiceBound = false;
        if (!isPlaying()) {
            stopSelf();
        }
        return true;
    }


    @Override
    public void onCreate() {
        initBroadcastReceiver();
        connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        handleIntent(intent);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        stop();
        if (socket != null) {
            socket.disconnect();
        }

        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(intentReceiver);

        super.onDestroy();
    }

    private void handleIntent(Intent intent) {
        final String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case StreamService.PLAY_PAUSE:
                    togglePlayPause();
                    break;

                case StreamService.STOP:
                    stop();
                    break;

                case StreamService.TOGGLE_FAVORITE:
                    favoriteCurrentSong();
                    break;

                // TODO: should pause when headphones unplugged
                case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                    if (player != null) {
                        player.setPlayWhenReady(false);
                    }
                    break;

                case MainActivity.AUTH_EVENT:
                    break;
            }
        }

        updateNotification();
    }

    private void initBroadcastReceiver() {
        intentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleIntent(intent);
            }
        };

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        intentFilter.addAction(MainActivity.AUTH_EVENT);
        intentFilter.addAction(StreamService.PLAY_PAUSE);
        intentFilter.addAction(StreamService.STOP);
        intentFilter.addAction(StreamService.TOGGLE_FAVORITE);

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(intentReceiver, intentFilter);
    }

    public boolean isStreamStarted() {
        return player != null;
    }

    /**
     * Gets the status of the music player and whether or not it's currently playing something.
     *
     * @return Whether the player is playing something.
     */
    public boolean isPlaying() {
        return player != null && player.getPlayWhenReady();
    }

    /**
     * Toggles the stream's play state.
     */
    private void togglePlayPause() {
        if (player == null) {
            startStream();
        } else if (player.getPlayWhenReady()) {
            player.setPlayWhenReady(false);
        } else {
            player.setPlayWhenReady(true);
            player.seekToDefaultPosition();
        }

        App.STATE.playing.set(isPlaying());
        updateNotification();
    }

    /**
     * Stops the stream and kills the service.
     */
    private void stop() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player = null;
        }

        stopForeground(true);
        stopSelf();

        App.STATE.playing.set(false);
    }

    private void favoriteCurrentSong() {
        final Song currentSong = App.STATE.currentSong.get();
        if (currentSong == null) return;

        final int songId = currentSong.getId();
        if (songId == -1) return;

        if (!AuthUtil.isAuthenticated(getApplicationContext())) {
            promptLogin();
            return;
        }

        APIUtil.favoriteSong(getApplicationContext(), songId, new FavoriteSongListener() {
            @Override
            public void onFailure(final String result) {
                if (result.equals(ResponseMessages.AUTH_FAILURE)) {
                    // TODO: should favorite after logging in
                    promptLogin();
                }
            }

            @Override
            public void onSuccess(final boolean favorited) {
                final Song currentSong = App.STATE.currentSong.get();
                if (currentSong.getId() == songId) {
                    currentSong.setFavorite(favorited);
                    App.STATE.currentFavorited.set(favorited);
                }

                updateNotification();
            }
        });
    }

    /**
     * Updates the notification if there is a song playing.
     */
    private void updateNotification() {
        final Song currentSong = App.STATE.currentSong.get();
        if (currentSong != null && currentSong.getId() != -1) {
            if (notification == null) {
                notification = new AppNotification(this);
            }

            notification.update();
        } else {
            stopForeground(true);
        }
    }

    /**
     * Opens up the login dialog in MainActivity.
     */
    private void promptLogin() {
        final Intent loginIntent = new Intent(MainActivity.TRIGGER_LOGIN);
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(loginIntent);
    }

    /**
     * Connects to the websocket and retrieves playback info.
     */
    public void connect() {
        disconnect();

        if (!NetworkUtil.isNetworkAvailable(getBaseContext())) {
            return;
        }

        try {
            socket = new WebSocketFactory().createSocket(Endpoints.SOCKET, SOCKET_TIMEOUT);
            socket.addListener(new WebSocketAdapter() {
                @Override
                public void onFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    // TODO: clean this up
                    if (frame.getPayloadText().contains("listeners")) {
                        // Get user token from shared preferences if socket not authenticated
                        if (!frame.getPayloadText().contains("\"extended\":{")) {
                            final String authToken = AuthUtil.getAuthToken(getBaseContext());
                            if (authToken != null) {
                                socket.sendText("{\"token\":\"" + authToken + "\"}");
                            }
                        }

                        // Parses the API information
                        parseWebSocketResponse(frame.getPayloadText());
                    }
                }

                @Override
                public void onConnectError(WebSocket websocket, WebSocketException e) throws Exception {
                    e.printStackTrace();
                    reconnect();
                }

                @Override
                public void onMessageDecompressionError(WebSocket websocket, WebSocketException e, byte[] compressed) throws Exception {
                    e.printStackTrace();
                }

                @Override
                public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                    if (closedByServer) {
                        reconnect();
                    } else {
                        stopSelf();
                    }
                }
            });

            socket.connectAsynchronously();
            socket.sendText("update");
        } catch (IOException e) {
            e.printStackTrace();
            reconnect();
        }
    }

    private void disconnect() {
        if (socket != null && socket.isOpen()) {
            socket.disconnect();
        }
        socket = null;
        parseWebSocketResponse(null);
    }

    private void reconnect() {
        disconnect();
        SystemClock.sleep(RETRY_TIME);
        connect();
    }

    private void parseWebSocketResponse(final String jsonString) {
        if (jsonString == null) {
            App.STATE.currentSong.set(null);
            App.STATE.listeners.set(0);
            App.STATE.requester.set(null);
        } else {
            final PlaybackInfo playbackInfo = GSON.fromJson(jsonString, PlaybackInfo.class);

            if (playbackInfo.getSongId() != 0) {
                App.STATE.currentSong.set(new Song(
                        playbackInfo.getSongId(),
                        playbackInfo.getArtistName().trim(),
                        playbackInfo.getSongName().trim(),
                        playbackInfo.getAnimeName().trim()
                ));

                // TODO: clean up how favorited track is handled
                if (playbackInfo.hasExtended()) {
                    final boolean favorited = playbackInfo.getExtended().isFavorite();
                    App.STATE.currentSong.get().setFavorite(favorited);
                    App.STATE.currentFavorited.set(favorited);
                }
            } else {
                App.STATE.currentSong.set(null);
            }

            App.STATE.listeners.set(playbackInfo.getListeners());
            App.STATE.requester.set(playbackInfo.getRequestedBy());
        }

        updateNotification();
    }

    /**
     * Creates and starts the stream.
     */
    private void startStream() {
        final BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        final TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        final TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        final LoadControl loadControl = new DefaultLoadControl();
        final DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "LISTEN.moe"));
        final ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        final MediaSource streamSource = new ExtractorMediaSource(Uri.parse(Endpoints.STREAM), dataSourceFactory, extractorsFactory, null, null);

        player = ExoPlayerFactory.newSimpleInstance(getApplicationContext(), trackSelector, loadControl);
        player.prepare(streamSource);
        player.setPlayWhenReady(true);

        player.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                // Try to reconnect to the stream
                player.release();
                player = null;
                reconnect();
                startStream();
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
            }

            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            }

            @Override
            public void onPositionDiscontinuity() {
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            }
        });
    }
}