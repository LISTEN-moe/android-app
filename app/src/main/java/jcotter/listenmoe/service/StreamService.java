package jcotter.listenmoe.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

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

import jcotter.listenmoe.R;
import jcotter.listenmoe.constants.Endpoints;
import jcotter.listenmoe.interfaces.FavoriteSongCallback;
import jcotter.listenmoe.model.PlaybackInfo;
import jcotter.listenmoe.model.Song;
import jcotter.listenmoe.ui.MenuActivity;
import jcotter.listenmoe.ui.RadioActivity;
import jcotter.listenmoe.util.APIUtil;
import jcotter.listenmoe.util.AuthUtil;

public class StreamService extends Service {

    public static final String UPDATE_PLAYING = "update_playing";
    public static final String UPDATE_PLAYING_SONG = UPDATE_PLAYING + ".song";
    public static final String UPDATE_PLAYING_LISTENERS = UPDATE_PLAYING + ".listeners";
    public static final String UPDATE_PLAYING_REQUESTER = UPDATE_PLAYING + ".requester";

    public static final String VOLUME = "volume";
    public static final String RECEIVER = "receiver";
    public static final String KILLABLE = "killable";
    public static final String REQUEST = "re:re";
    public static final String PLAY = "play";
    public static final String RUNNING = "running";
    public static final String STOP = "stop";
    public static final String FAVORITE = "favorite";
    public static final String TOGGLE_FAVORITE = "favUpdate";
    public static final String PROBE = "probe";

    public static boolean isServiceRunning = false;


    private SimpleExoPlayer voiceOfKanacchi;
    private WebSocket ws;
    private boolean uiOpen;
    private boolean notif;
    private int notifID;

    private Gson gson;
    private Song currentSong;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        this.gson = new Gson();

        uiOpen = true;

        notif = false;
        notifID = -1;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        isServiceRunning = true;

        // Volume control
        if (intent.hasExtra(StreamService.VOLUME)) {
            if (voiceOfKanacchi != null) {
                voiceOfKanacchi.setVolume(intent.getFloatExtra(StreamService.VOLUME, 0.5f));
            }
        }

        // Starts WebSocket
        if (intent.hasExtra(StreamService.RECEIVER)) {
            connectWebSocket();
        } else {
            // Allows service to be killed
            if (intent.hasExtra(StreamService.KILLABLE)) {
                uiOpen = false;
                if (voiceOfKanacchi != null && !voiceOfKanacchi.getPlayWhenReady()) {
                    stopForeground(true);
                    stopSelf();
                }
            } else {
                // Requests WebSocket update
                if (intent.hasExtra(StreamService.REQUEST)) {
                    uiOpen = true;
                    final String authToken = AuthUtil.getAuthToken(getApplicationContext());
                    if (authToken == null && ws != null) {
                        ws.sendText("update");
                    } else if (ws != null) {
                        ws.sendText("{\"token\":\"" + authToken + "\"}");
                    } else {
                        connectWebSocket();
                    }
                } else {
                    // Play/pause music stream
                    if (intent.hasExtra(StreamService.PLAY)) {
                        final Intent returnIntent = new Intent(getPackageName());
                        if (intent.getBooleanExtra(StreamService.PLAY, false)) {
                            if (voiceOfKanacchi == null) {
                                startStream();
                            } else {
                                voiceOfKanacchi.setPlayWhenReady(true);
                                voiceOfKanacchi.seekToDefaultPosition();
                            }
                            returnIntent.putExtra(StreamService.RUNNING, true);
                        } else {
                            voiceOfKanacchi.setPlayWhenReady(false);
                            returnIntent.putExtra(StreamService.RUNNING, false);
                        }
                        sendBroadcast(returnIntent);
                    } else {
                        // Stop Stream & Foreground ( & Service (Depends))
                        if (intent.hasExtra(StreamService.STOP)) {
                            notif = false;

                            voiceOfKanacchi.setPlayWhenReady(false);
                            stopForeground(true);
                            if (!uiOpen) {
                                stopSelf();
                            }

                            final Intent returnIntent = new Intent(getPackageName());
                            returnIntent.putExtra(StreamService.RUNNING, false);
                            sendBroadcast(returnIntent);
                        } else {
                            // Toggle favorite status of current song
                            if (intent.hasExtra(StreamService.FAVORITE)) {
                                APIUtil.favoriteSong(getApplicationContext(), currentSong.getId(), new FavoriteSongCallback() {
                                    @Override
                                    public void onFailure(final String result) {
                                    }

                                    @Override
                                    public void onSuccess(final boolean favorited) {
                                        currentSong.setFavorite(favorited);

                                        if (uiOpen) {
                                            final Intent favIntent = new Intent(getPackageName());
                                            favIntent.putExtra(StreamService.FAVORITE, favorited);
                                            sendBroadcast(favIntent);
                                        }

                                        notification();
                                    }
                                });
                            } else if (intent.hasExtra(StreamService.TOGGLE_FAVORITE)) {
                                currentSong.setFavorite(intent.getBooleanExtra(StreamService.TOGGLE_FAVORITE, false));
                            }
                        }
                    }
                }
            }
        }

        // Returns music stream state to RadioActivity
        if (intent.hasExtra(StreamService.PROBE)) {
            final Intent returnIntent = new Intent(getPackageName());
            returnIntent.putExtra(StreamService.RUNNING, voiceOfKanacchi != null && voiceOfKanacchi.getPlayWhenReady());
            sendBroadcast(returnIntent);
        }

        // Update notification
        notification();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        isServiceRunning = false;

        if (ws != null) {
            ws.disconnect();
        }
    }


    // WEBSOCKET METHODS

    /**
     * Connects to the websocket and retrieves playback info.
     */
    private void connectWebSocket() {
        final String url = Endpoints.SOCKET;
        // Create Web Socket
        ws = null;
        final WebSocketFactory factory = new WebSocketFactory();
        try {
            ws = factory.createSocket(url, 900000);
            ws.addListener(new WebSocketAdapter() {
                @Override
                public void onFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    if (frame.getPayloadText().contains("listeners")) {
                        // Get userToken from shared preferences if socket not authenticated //
                        if (!frame.getPayloadText().contains("\"extended\":{")) {
                            final String authToken = AuthUtil.getAuthToken(getBaseContext());
                            if (authToken != null) {
                                ws.sendText("{\"token\":\"" + authToken + "\"}");
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
                    connectWebSocket();
                }

                @Override
                public void onMessageDecompressionError(WebSocket websocket, WebSocketException cause, byte[] compressed) throws Exception {
                    cause.printStackTrace();
                }

                @Override
                public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                    SystemClock.sleep(6000);
                    if (closedByServer)
                        connectWebSocket();
                    else
                        stopSelf();
                }
            });
            // Connect to the socket
            ws.connectAsynchronously();
        } catch (IOException ex) {
            ex.printStackTrace();
            parseJSON("NULL");
            if (ws.isOpen()) {
                ws.disconnect();
            }
            connectWebSocket();
        }
    }

    /**
     * Parses JSON resposne from websocket.
     *
     * @param jsonString Response from the LISTEN.moe websocket.
     */
    private void parseJSON(String jsonString) {
        PlaybackInfo playbackInfo = gson.fromJson(jsonString, PlaybackInfo.class);

        if (playbackInfo.getSongId() != 0) {
            currentSong = new Song(
                    playbackInfo.getSongId(),
                    playbackInfo.getArtistName().trim(),
                    playbackInfo.getSongName().trim(),
                    playbackInfo.getAnimeName().trim()
            );

            if (playbackInfo.hasExtended()) {
                currentSong.setFavorite(playbackInfo.getExtended().isFavorite());
            }
        } else {
            currentSong = null;
        }

        // Send the updated info to the RadioActivity
        final Intent intent = new Intent();
        intent.setAction(StreamService.UPDATE_PLAYING);
        intent.putExtra(StreamService.UPDATE_PLAYING_SONG, currentSong);
        intent.putExtra(StreamService.UPDATE_PLAYING_LISTENERS, playbackInfo.getListeners());
        intent.putExtra(StreamService.UPDATE_PLAYING_REQUESTER, playbackInfo.getRequestedBy());
        sendBroadcast(intent);

        // Update notification
        notification();
    }

    /**
     * Creates a notification for the foreground service.
     */
    private void notification() {
        if (!notif) return;

        if (notifID == -1)
            notifID = (int) System.currentTimeMillis();

        final Intent intent = new Intent(this, RadioActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(currentSong.getArtist())
                .setSmallIcon(R.drawable.icon_notification)
                .setContentIntent(pendingIntent)
                .setColor(Color.argb(255, 29, 33, 50));

        // Construct string with song title and anime
        final String currentSongAnime = currentSong.getAnime();
        String title = currentSong.getTitle();
        if (!currentSongAnime.equals("")) {
            title += "\n" + "[" + currentSongAnime + "]";
        }
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(title));
        builder.setContentText(title);

        // Play/pause button
        final Intent playPauseIntent = new Intent(this, this.getClass());
        PendingIntent playPausePending;
        if (voiceOfKanacchi.getPlayWhenReady()) {
            playPauseIntent.putExtra(StreamService.PLAY, false);
            playPausePending = PendingIntent.getService(this, 1, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
                builder.addAction(new NotificationCompat.Action.Builder(R.drawable.icon_pause, "", playPausePending).build());
            else
                builder.addAction(new NotificationCompat.Action.Builder(R.drawable.icon_pause, getString(R.string.action_pause), playPausePending).build());
        } else {
            playPauseIntent.putExtra(StreamService.PLAY, true);
            playPausePending = PendingIntent.getService(this, 1, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
                builder.addAction(new NotificationCompat.Action.Builder(R.drawable.icon_play, "", playPausePending).build());
            else
                builder.addAction(new NotificationCompat.Action.Builder(R.drawable.icon_play, getString(R.string.action_play), playPausePending).build());
        }

        // Favorite button
        final Intent favoriteIntent = new Intent(this, this.getClass());
        favoriteIntent.putExtra(StreamService.FAVORITE, true);
        PendingIntent favoritePending = PendingIntent.getService(this, 2, favoriteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (!AuthUtil.isAuthenticated(getApplicationContext())) {
            final Intent authIntent = new Intent(this, MenuActivity.class);
            authIntent.putExtra("index", 2);
            PendingIntent authPending = PendingIntent.getActivity(this, 3, authIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
                builder.addAction(new NotificationCompat.Action.Builder(R.drawable.favorite_empty, "", authPending).build());
            else
                builder.addAction(new NotificationCompat.Action.Builder(R.drawable.favorite_empty, getString(R.string.action_favorite), authPending).build());
        } else {
            if (currentSong.isFavorite())
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
                    builder.addAction(new NotificationCompat.Action.Builder(R.drawable.favorite_full, "", favoritePending).build());
                else
                    builder.addAction(new NotificationCompat.Action.Builder(R.drawable.favorite_full, getString(R.string.action_unfavorite), favoritePending).build());
            else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
                builder.addAction(new NotificationCompat.Action.Builder(R.drawable.favorite_empty, "", favoritePending).build());
            else
                builder.addAction(new NotificationCompat.Action.Builder(R.drawable.favorite_empty, getString(R.string.action_favorite), favoritePending).build());
        }

        // Stop button
        final Intent stopIntent = new Intent(this, this.getClass());
        stopIntent.putExtra(StreamService.STOP, true);
        PendingIntent stopPending = PendingIntent.getService(this, 4, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            builder.addAction(new NotificationCompat.Action.Builder(R.drawable.icon_close, "", stopPending).build());
        else
            builder.addAction(new NotificationCompat.Action.Builder(R.drawable.icon_close, getString(R.string.action_stop), stopPending).build());

        startForeground(notifID, builder.build());
    }


    // MUSIC PLAYER RELATED METHODS //

    /**
     * Creates and starts the stream.
     */
    private void startStream() {
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();
        voiceOfKanacchi = ExoPlayerFactory.newSimpleInstance(getApplicationContext(), trackSelector, loadControl);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "LISTEN.moe"));
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource streamSource = new ExtractorMediaSource(Uri.parse(Endpoints.STREAM), dataSourceFactory, extractorsFactory, null, null);
        streamListener();
        voiceOfKanacchi.prepare(streamSource);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        voiceOfKanacchi.setVolume(sharedPreferences.getFloat(StreamService.VOLUME, 0.5f));
        voiceOfKanacchi.setPlayWhenReady(true);
        notif = true;
    }

    /**
     * Restarts the stream if a disconnect occurs.
     */
    private void streamListener() {
        voiceOfKanacchi.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                voiceOfKanacchi.release();
                voiceOfKanacchi = null;
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