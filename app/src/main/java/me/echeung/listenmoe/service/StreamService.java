package me.echeung.listenmoe.service;

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

import me.echeung.listenmoe.constants.Endpoints;
import me.echeung.listenmoe.interfaces.FavoriteSongListener;
import me.echeung.listenmoe.model.PlaybackInfo;
import me.echeung.listenmoe.model.Song;
import me.echeung.listenmoe.service.notification.AppNotification;
import me.echeung.listenmoe.ui.App;
import me.echeung.listenmoe.ui.activities.MainActivity;
import me.echeung.listenmoe.util.APIUtil;
import me.echeung.listenmoe.util.AuthUtil;

public class StreamService extends Service {

    public static final String REQUEST = "re:re";
    public static final String PLAY = "play";
    public static final String STOP = "stop";
    public static final String FAVORITE = "favorite";

    private SimpleExoPlayer player;
    private WebSocket socket;
    private boolean uiOpen;

    private AppNotification notification;

    private final IBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        public StreamService getService() {
            return StreamService.this;
        }
    }

    private static final Gson GSON = new Gson();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        notification = new AppNotification(this);

        uiOpen = true;

        connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        // Requests WebSocket update
        // TODO: when am I supposed to do this?
        if (intent.hasExtra(StreamService.REQUEST)) {
            uiOpen = true;
            final String authToken = AuthUtil.getAuthToken(getApplicationContext());
            if (authToken == null && socket != null) {
                socket.sendText("update");
            } else if (socket != null) {
                socket.sendText("{\"token\":\"" + authToken + "\"}");
            } else {
                connect();
            }
        }

        if (intent.hasExtra(StreamService.PLAY)) {
            togglePlayPause();
        }

        if (intent.hasExtra(StreamService.STOP)) {
            stop();
        }

        // Toggle favorite status of current song
        if (intent.hasExtra(StreamService.FAVORITE)) {
            APIUtil.favoriteSong(getApplicationContext(), App.STATE.currentSong.get().getId(), new FavoriteSongListener() {
                @Override
                public void onFailure(final String result) {
                }

                @Override
                public void onSuccess(final boolean favorited) {
                    App.STATE.currentSong.get().setFavorite(favorited);

                    if (uiOpen) {
                        final Intent favIntent = new Intent(getPackageName());
                        favIntent.putExtra(StreamService.FAVORITE, favorited);
                        sendBroadcast(favIntent);
                    }

                    updateNotification();
                }
            });
        }

        updateNotification();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        stop();
        if (socket != null) {
            socket.disconnect();
        }

        super.onDestroy();
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
    public void togglePlayPause() {
        if (player != null && player.getPlayWhenReady()) {
            player.setPlayWhenReady(false);
        } else {
            if (player == null) {
                startStream();
            } else {
                player.setPlayWhenReady(true);
                player.seekToDefaultPosition();
            }
        }

        App.STATE.playing.set(isPlaying());
        updateNotification();
    }

    /**
     * Stops the stream and kills the service.
     */
    public void stop() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player = null;
        }

        stopForeground(true);
        if (!uiOpen) {
            stopSelf();
        }

        App.STATE.playing.set(false);
    }

    /**
     * Updates the notification if there is a song playing.
     */
    private void updateNotification() {
        final Song currentSong = App.STATE.currentSong.get();
        if (currentSong != null && currentSong.getId() != -1) {
            notification.update();
        }
    }

    /**
     * Connects to the websocket and retrieves playback info.
     */
    public void connect() {
        if (socket != null) {
            socket.disconnect();
            socket = null;
        }

        final WebSocketFactory factory = new WebSocketFactory();
        try {
            socket = factory.createSocket(Endpoints.SOCKET, 900000);
            socket.addListener(new WebSocketAdapter() {
                @Override
                public void onFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    if (frame.getPayloadText().contains("listeners")) {
                        // Get userToken from shared preferences if socket not authenticated //
                        if (!frame.getPayloadText().contains("\"extended\":{")) {
                            final String authToken = AuthUtil.getAuthToken(getBaseContext());
                            if (authToken != null) {
                                socket.sendText("{\"token\":\"" + authToken + "\"}");
                            }
                        }

                        // Parses the API information
                        parseJSON(frame.getPayloadText());
                    }
                }

                @Override
                public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
                    exception.printStackTrace();
                    parseJSON("NULL");
                    SystemClock.sleep(6000);
                    connect();
                }

                @Override
                public void onMessageDecompressionError(WebSocket websocket, WebSocketException cause, byte[] compressed) throws Exception {
                    cause.printStackTrace();
                }

                @Override
                public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                    SystemClock.sleep(6000);
                    if (closedByServer)
                        connect();
                    else
                        stopSelf();
                }
            });

            socket.connectAsynchronously();
        } catch (IOException ex) {
            ex.printStackTrace();
            parseJSON("NULL");  // TODO
            if (socket.isOpen()) {
                socket.disconnect();
            }
            connect();
        }
    }

    /**
     * Parses JSON resposne from websocket.
     *
     * @param jsonString Response from the LISTEN.moe websocket.
     */
    private void parseJSON(final String jsonString) {
        final PlaybackInfo playbackInfo = GSON.fromJson(jsonString, PlaybackInfo.class);

        if (playbackInfo.getSongId() != 0) {
            App.STATE.currentSong.set(new Song(
                    playbackInfo.getSongId(),
                    playbackInfo.getArtistName().trim(),
                    playbackInfo.getSongName().trim(),
                    playbackInfo.getAnimeName().trim()
            ));

            if (playbackInfo.hasExtended()) {
                App.STATE.currentSong.get().setFavorite(playbackInfo.getExtended().isFavorite());
            }
        } else {
            App.STATE.currentSong.set(null);
        }

        App.STATE.listeners.set(playbackInfo.getListeners());
        App.STATE.requester.set(playbackInfo.getRequestedBy());

        // Update notification
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

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        intentFilter.addAction(MainActivity.AUTH_EVENT);

        final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                        player.setPlayWhenReady(false);
                        updateNotification();
                        break;

                    case MainActivity.AUTH_EVENT:
                        updateNotification();
                        break;
                }
            }
        };

        player.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                player.release();
                player = null;
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
                try {
                    if (playWhenReady) {
                        registerReceiver(broadcastReceiver, intentFilter);
                    } else {
                        unregisterReceiver(broadcastReceiver);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        });
    }
}