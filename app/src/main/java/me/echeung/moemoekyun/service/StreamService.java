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
import android.view.KeyEvent;

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
import com.google.gson.Gson;

import me.echeung.moemoekyun.constants.Endpoints;
import me.echeung.moemoekyun.constants.ResponseMessages;
import me.echeung.moemoekyun.interfaces.FavoriteSongListener;
import me.echeung.moemoekyun.model.PlaybackInfo;
import me.echeung.moemoekyun.model.Song;
import me.echeung.moemoekyun.service.notification.AppNotification;
import me.echeung.moemoekyun.state.AppState;
import me.echeung.moemoekyun.state.UserState;
import me.echeung.moemoekyun.ui.activities.MainActivity;
import me.echeung.moemoekyun.ui.fragments.UserFragment;
import me.echeung.moemoekyun.util.APIUtil;
import me.echeung.moemoekyun.util.AuthUtil;
import me.echeung.moemoekyun.util.NetworkUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class StreamService extends Service {

    public static final String PLAY_PAUSE = "play_pause";
    public static final String STOP = "stop";
    public static final String TOGGLE_FAVORITE = "toggle_favorite";
    public static final String UPDATE = "update";

    private static final Gson GSON = new Gson();

    private final IBinder binder = new ServiceBinder();
    private boolean isServiceBound = false;

    private AppNotification notification;

    private AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;
    private boolean wasPlayingBeforeLoss;

    private SimpleExoPlayer player;
    private RadioSocket radioSocket;

    private BroadcastReceiver intentReceiver;
    private boolean receiverRegistered = false;

    @Override
    public IBinder onBind(Intent intent) {
        isServiceBound = true;
        return binder;
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
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioFocusChangeListener = focusChange -> {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (player != null) {
                        player.setVolume(1f);
                    }
                    if (wasPlayingBeforeLoss) {
                        play();
                    }
                    break;

                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    wasPlayingBeforeLoss = isPlaying();
                    if (wasPlayingBeforeLoss) {
                        pause();
                    }
                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    wasPlayingBeforeLoss = isPlaying();
                    if (player != null) {
                        player.setVolume(0.5f);
                    }
                    break;
            }
        };

        initBroadcastReceiver();

        radioSocket = new RadioSocket();
        radioSocket.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        handleIntent(intent);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        stop();
        radioSocket.disconnect();

        if (receiverRegistered) {
            unregisterReceiver(intentReceiver);
            receiverRegistered = false;
        }

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

                case StreamService.UPDATE:
                    radioSocket.update();
                    break;

                // Pause when headphones unplugged
                case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                    pause();
                    break;

                // Headphone media button action
                case Intent.ACTION_MEDIA_BUTTON:
                    final KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
                    if (keyEvent == null || keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
                        return;
                    }

                    switch (keyEvent.getKeyCode()) {
                        case KeyEvent.KEYCODE_HEADSETHOOK:
                        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                            togglePlayPause();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PLAY:
                            play();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PAUSE:
                            pause();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_STOP:
                            stop();
                            break;
                    }
                    break;

                case MainActivity.AUTH_EVENT:
                    if (!AuthUtil.isAuthenticated(this)) {
                        AppState.getInstance().setFavorited(false);
                        updateNotification();
                    } else {
                        radioSocket.update();
                    }
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
        intentFilter.addAction(StreamService.PLAY_PAUSE);
        intentFilter.addAction(StreamService.STOP);
        intentFilter.addAction(StreamService.TOGGLE_FAVORITE);
        intentFilter.addAction(StreamService.UPDATE);
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        intentFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
        intentFilter.addAction(MainActivity.AUTH_EVENT);

        registerReceiver(intentReceiver, intentFilter);
        receiverRegistered = true;
    }

    public boolean isStreamStarted() {
        return player != null;
    }

    public boolean isPlaying() {
        return player != null && player.getPlayWhenReady();
    }

    private void togglePlayPause() {
        if (isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    private void play() {
        if (player == null) {
            initStream();
        }

        if (!isPlaying()) {
            // Request audio focus for playback
            int result = audioManager.requestAudioFocus(audioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                player.setPlayWhenReady(true);
                player.seekToDefaultPosition();

                AppState.getInstance().playing.set(true);
                updateNotification();
            }
        }
    }

    private void pause() {
        if (isPlaying()) {
            player.setPlayWhenReady(false);

            AppState.getInstance().playing.set(false);
            updateNotification();
        }
    }

    private void stop() {
        if (isPlaying()) {
            audioManager.abandonAudioFocus(audioFocusChangeListener);
        }

        if (player != null) {
            player.setPlayWhenReady(false);
            player = null;
        }

        stopForeground(true);
        stopSelf();

        AppState.getInstance().playing.set(false);
    }

    private void favoriteCurrentSong() {
        final Song currentSong = AppState.getInstance().currentSong.get();
        if (currentSong == null) return;

        final int songId = currentSong.getId();
        if (songId == -1) return;

        if (!AuthUtil.isAuthenticated(getApplicationContext())) {
            promptLoginFavorite();
            return;
        }

        APIUtil.favoriteSong(getApplicationContext(), songId, new FavoriteSongListener() {
            @Override
            public void onFailure(final String result) {
                if (result.equals(ResponseMessages.AUTH_FAILURE)) {
                    promptLoginFavorite();
                }
            }

            @Override
            public void onSuccess(final boolean favorited) {
                final Song currentSong = AppState.getInstance().currentSong.get();
                if (currentSong.getId() == songId) {
                    AppState.getInstance().setFavorited(favorited);
                }

                final Intent favIntent = new Intent(UserFragment.FAVORITE_EVENT);
                sendBroadcast(favIntent);

                updateNotification();
            }
        });
    }

    private void updateNotification() {
        final Song currentSong = AppState.getInstance().currentSong.get();
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
    private void promptLoginFavorite() {
        final Intent loginIntent = new Intent(MainActivity.TRIGGER_LOGIN_AND_FAVORITE);
        sendBroadcast(loginIntent);
    }

    /**
     * Creates and starts the stream.
     */
    private void initStream() {
        final BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        final TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        final TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        final LoadControl loadControl = new DefaultLoadControl();
        final DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, getPackageName()));
        final ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        final RenderersFactory renderersFactory = new DefaultRenderersFactory(getApplicationContext());
        final MediaSource streamSource = new ExtractorMediaSource(Uri.parse(Endpoints.STREAM), dataSourceFactory, extractorsFactory, null, null);

        player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);
        player.prepare(streamSource);

        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                // Try to reconnect to the stream
                final boolean wasPlaying = isPlaying();

                player.release();
                player = null;

                initStream();
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

    private final class RadioSocket extends WebSocketListener {

        private static final int RETRY_TIME_MIN = 250;
        private static final int RETRY_TIME_MAX = 4000;

        private WebSocket socket;
        private int retryTime = RETRY_TIME_MIN;

        void connect() {
            if (!NetworkUtil.isNetworkAvailable(getBaseContext())) {
                return;
            }

            final Request request = new Request.Builder().url(Endpoints.SOCKET).build();
            socket = new OkHttpClient().newWebSocket(request, this);
        }

        void disconnect() {
            if (socket != null) {
                socket.cancel();
                socket = null;
            }
            parseWebSocketResponse(null);
        }

        void reconnect() {
            disconnect();

            // Exponential backoff
            SystemClock.sleep(retryTime);
            if (retryTime < RETRY_TIME_MAX) {
                retryTime *= 2;
            }

            connect();
        }

        void update() {
            if (socket == null) {
                connect();
            }

            if (AuthUtil.isAuthenticated(getBaseContext())) {
                final String authToken = AuthUtil.getAuthToken(getBaseContext());
                if (authToken != null) {
                    socket.send("{\"token\":\"" + authToken + "\"}");
                }
            }
        }

        @Override
        public void onOpen(WebSocket socket, Response response) {
            retryTime = RETRY_TIME_MIN;

            // This actually causes a MALFORMED-JSON error
            // socket.send("update");
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            // TODO: clean this up
            if (text.contains("\"listeners\":")) {
                // Get user token from shared preferences if socket not authenticated
                if (!text.contains("\"extended\":")) {
                    update();
                }

                parseWebSocketResponse(text);
            } else if (text.contains("\"reason\":")) {
                // We get a "CLEANUP" disconnect message after a while
                reconnect();
            }
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            t.printStackTrace();
            reconnect();
        }

        @Override
        public void	onClosed(WebSocket webSocket, int code, String reason) {
            reconnect();
        }

        private void parseWebSocketResponse(final String jsonString) {
            final AppState state = AppState.getInstance();

            if (jsonString == null) {
                state.currentSong.set(null);
                state.lastSong.set(null);
                state.secondLastSong.set(null);

                state.listeners.set(0);
                state.requester.set(null);
            } else {
                final PlaybackInfo playbackInfo = GSON.fromJson(jsonString, PlaybackInfo.class);

                if (playbackInfo.getSongId() != 0) {
                    state.currentSong.set(new Song(
                            playbackInfo.getSongId(),
                            playbackInfo.getArtistName().trim(),
                            playbackInfo.getSongName().trim(),
                            playbackInfo.getAnimeName().trim()
                    ));

                    state.lastSong.set(playbackInfo.getLast().toString());
                    state.secondLastSong.set(playbackInfo.getSecondLast().toString());

                    if (playbackInfo.hasExtended()) {
                        final PlaybackInfo.ExtendedInfo extended = playbackInfo.getExtended();

                        state.setFavorited(extended.isFavorite());

                        UserState.getInstance().queueSize.set(extended.getQueue().getSongsInQueue());
                        UserState.getInstance().queuePosition.set(extended.getQueue().getInQueueBeforeUserSong());
                    }

                    // TODO: Last.fm scrobbling/musicxmatch
                }

                state.listeners.set(playbackInfo.getListeners());
                state.requester.set(playbackInfo.getRequestedBy());
            }

            updateNotification();
        }
    }

    public class ServiceBinder extends Binder {
        public StreamService getService() {
            return StreamService.this;
        }
    }
}