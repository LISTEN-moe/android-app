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
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import jcotter.listenmoe.R;
import jcotter.listenmoe.constants.Endpoints;
import jcotter.listenmoe.constants.SocketResponse;
import jcotter.listenmoe.interfaces.FavoriteSongCallback;
import jcotter.listenmoe.ui.MenuActivity;
import jcotter.listenmoe.ui.RadioActivity;
import jcotter.listenmoe.util.APIUtil;

public class StreamService extends Service {

    private SimpleExoPlayer voiceOfKanacchi;
    private WebSocket ws;
    private float volume;
    private String artist;
    private String title;
    private String anime;
    private int songID;
    private boolean favorite;
    private boolean uiOpen;
    private boolean notif;
    private int notifID;

    public StreamService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        uiOpen = true;
        volume = 0.5f;
        notif = false;
        notifID = -1;
    }

    @Override
    public void onDestroy() {
        if (ws != null)
            ws.disconnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        // Volume Control //
        if (intent.hasExtra("volume")) {
            volume = intent.getFloatExtra("volume", 0.5f);
            if (voiceOfKanacchi != null)
                voiceOfKanacchi.setVolume(intent.getFloatExtra("volume", 0.5f));
        }
        // Starts WebSocket //
        if (intent.hasExtra("receiver"))
            connectWebSocket();
        else
            // Allows Service to be Killed //
            if (intent.hasExtra("killable")) {
                uiOpen = false;
                if (voiceOfKanacchi != null && !voiceOfKanacchi.getPlayWhenReady()) {
                    stopForeground(true);
                    stopSelf();
                }
            } else
                // Requests WebSocket Update //
                if (intent.hasExtra("re:re")) {
                    uiOpen = true;
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    if (sharedPreferences.getString("userToken", "NULL").equals("NULL") && ws != null)
                        ws.sendText("update");
                    else if (ws != null)
                        ws.sendText("{\"token\":\"" + sharedPreferences.getString("userToken", "NULL") + "\"}");
                    else
                        connectWebSocket();
                } else
                    // Play/Pause Music Stream //
                    if (intent.hasExtra("play")) {
                        Intent returnIntent = new Intent("jcotter.listenmoe");
                        if (intent.getBooleanExtra("play", false)) {
                            if (voiceOfKanacchi == null) {
                                startStream();
                                returnIntent.putExtra("running", true);
                            } else {
                                voiceOfKanacchi.setPlayWhenReady(true);
                                voiceOfKanacchi.seekToDefaultPosition();
                                returnIntent.putExtra("running", true);
                            }
                        } else {
                            voiceOfKanacchi.setPlayWhenReady(false);
                            returnIntent.putExtra("running", false);
                        }
                        sendBroadcast(returnIntent);
                    } else
                        // Stop Stream & Foreground ( & Service (Depends)) //
                        if (intent.hasExtra("stop")) {
                            notif = false;
                            voiceOfKanacchi.setPlayWhenReady(false);
                            stopForeground(true);
                            if (!uiOpen) {
                                stopSelf();
                            }
                            Intent returnIntent = new Intent("jcotter.listenmoe")
                                    .putExtra("running", false);
                            sendBroadcast(returnIntent);
                        } else
                            // Change Favorite Status of Current Song //
                            if (intent.hasExtra("favorite")) {
                                APIUtil apiUtil = new APIUtil(getApplicationContext());
                                apiUtil.favoriteSong(songID, new FavoriteSongCallback() {
                                    @Override
                                    public void onFailure(String result) {

                                    }

                                    @Override
                                    public void onSuccess(String jsonResult) {
                                        if (jsonResult.contains("success\":true")) {
                                            favorite = jsonResult.contains("favorite\":true");

                                            if (uiOpen) {
                                                Intent favIntent = new Intent("jcotter.listenmoe")
                                                        .putExtra("favorite", favorite);
                                                sendBroadcast(favIntent);
                                            }

                                            notification();
                                        }
                                    }
                                });
                            } else if (intent.hasExtra("favUpdate"))
                                favorite = intent.getBooleanExtra("favUpdate", false);
        // Returns Music Stream State to RadioInterface //
        if (intent.hasExtra("probe")) {
            Intent returnIntent = new Intent("jcotter.listenmoe");
            returnIntent.putExtra("volume", (int) (volume * 100));
            if (voiceOfKanacchi != null && voiceOfKanacchi.getPlayWhenReady())
                returnIntent.putExtra("running", true);
            else
                returnIntent.putExtra("running", false);
            sendBroadcast(returnIntent);
        }
        // Updates Notification //
        notification();

        return START_NOT_STICKY;
    }


    // WEBSOCKET RELATED METHODS //

    /**
     * Connects to the websocket and retrieves playback info.
     */
    private void connectWebSocket() {
        final String url = Endpoints.SOCKET;
        // Create Web Socket //
        ws = null;
        WebSocketFactory factory = new WebSocketFactory();
        try {
            ws = factory.createSocket(url, 900000);
            ws.addListener(new WebSocketAdapter() {
                @Override
                public void onFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    if (frame.getPayloadText().contains("listeners")) {
                        // Get userToken from shared preferences if socket not authenticated //
                        if (!frame.getPayloadText().contains("\"extended\":{")) {
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            String token = sharedPreferences.getString("userToken", "NULL");
                            if (!token.equals("NULL")) {
                                ws.sendText("{\"token\":\"" + token + "\"}");
                            }
                        }
                        // Parses the API information //
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
     * @param json Response from the LISTEN.moe websocket.
     */
    private void parseJSON(String json) {
        String nowPlaying = "NULL";
        String listeners = "NULL";
        String requestedBy = "NULL";
        favorite = false;
        songID = -1;
        if (json.contains("listeners")) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                listeners = getResources().getString(R.string.currentListeners);
                listeners = listeners + "  " + jsonObject.getString(SocketResponse.LISTENERS) + "  ";
                nowPlaying = getResources().getString(R.string.nowPlaying);
                songID = jsonObject.getInt(SocketResponse.SONG_ID);
                title = jsonObject.getString(SocketResponse.SONG_NAME).trim();
                artist = jsonObject.getString(SocketResponse.ARTIST_NAME).trim();
                anime = jsonObject.getString(SocketResponse.ANIME_NAME).trim();
                if (anime.equals("")) {
                    nowPlaying = nowPlaying + "\n" + artist + "\n" + title;
                    anime = "NULL";
                } else
                    nowPlaying = nowPlaying + "\n" + artist + "\n" + title + "\n[" + anime + "]";
                String requested_by = jsonObject.getString(SocketResponse.REQUESTED_BY);
                if (!requested_by.equals("")) {
                    String base = getResources().getString(R.string.requestedText);
                    requestedBy = base + " " + "<a href=\"https://forum.listen.moe/u/" + requested_by + "\"" + ">" + requested_by + "</a>";
                }
                if (json.contains("\"extended\":{")) {
                    JSONObject jsonObjectE = jsonObject.getJSONObject(SocketResponse.EXTENDED);
                    favorite = jsonObjectE.getBoolean(SocketResponse.EXTENDED_FAVORITE);
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        } else {
            nowPlaying = getResources().getString(R.string.apiFailed);
            listeners = getResources().getString(R.string.currentListeners) + " 0";
        }
        Intent intent = new Intent("jcotter.listenmoe")
                .putExtra("nowPlaying", nowPlaying)
                .putExtra("listeners", listeners)
                .putExtra("requestedBy", requestedBy)
                .putExtra("songID", songID)
                .putExtra("favorite", favorite)
                .putExtra("authenticated", json.contains("\"extended\":{"));
        sendBroadcast(intent);
        notification();
    }

    /**
     * Creates a notification for the foreground service.
     */
    private void notification() {
        if (!notif) return;
        if (notifID == -1)
            notifID = (int) System.currentTimeMillis();
        Intent intent = new Intent(this, RadioActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(artist)
                .setSmallIcon(R.drawable.icon_notification)
                .setContentIntent(pendingIntent)
                .setColor(Color.argb(255, 29, 33, 50));
        if (!anime.equals("NULL")) {
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(title + "\n" + "[" + anime + "]"));
            builder.setContentText(title + "\n" + "[" + anime + "]");
        } else {
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(title));
            builder.setContentText(title);
        }
        // Play Pause Button //
        Intent playPauseIntent = new Intent(this, this.getClass());
        PendingIntent playPausePending;
        if (voiceOfKanacchi.getPlayWhenReady()) {
            playPauseIntent.putExtra("play", false);
            playPausePending = PendingIntent.getService(this, 1, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT < 24)
                builder.addAction(new NotificationCompat.Action.Builder(R.drawable.icon_pause, "", playPausePending).build());
            else
                builder.addAction(new NotificationCompat.Action.Builder(R.drawable.icon_pause, "Pause", playPausePending).build());
        } else {
            playPauseIntent.putExtra("play", true);
            playPausePending = PendingIntent.getService(this, 1, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT < 24)
                builder.addAction(new NotificationCompat.Action.Builder(R.drawable.icon_play, "", playPausePending).build());
            else
                builder.addAction(new NotificationCompat.Action.Builder(R.drawable.icon_play, "Play", playPausePending).build());
        }
        // Favorite Button //
        Intent favoriteIntent = new Intent(this, this.getClass())
                .putExtra("favorite", true);
        PendingIntent favoritePending = PendingIntent.getService(this, 2, favoriteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (sharedPreferences.getString("userToken", "NULL").equals("NULL")) {
            Intent authIntent = new Intent(this, MenuActivity.class)
                    .putExtra("index", 2);
            PendingIntent authPending = PendingIntent.getActivity(this, 3, authIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT < 24)
                builder.addAction(new NotificationCompat.Action.Builder(R.drawable.favorite_empty, "", authPending).build());
            else
                builder.addAction(new NotificationCompat.Action.Builder(R.drawable.favorite_empty, "Favorite", authPending).build());
        } else {
            if (favorite)
                if (Build.VERSION.SDK_INT < 24)
                    builder.addAction(new NotificationCompat.Action.Builder(R.drawable.favorite_full, "", favoritePending).build());
                else
                    builder.addAction(new NotificationCompat.Action.Builder(R.drawable.favorite_full, "UnFavorite", favoritePending).build());
            else if (Build.VERSION.SDK_INT < 24)
                builder.addAction(new NotificationCompat.Action.Builder(R.drawable.favorite_empty, "", favoritePending).build());
            else
                builder.addAction(new NotificationCompat.Action.Builder(R.drawable.favorite_empty, "Favorite", favoritePending).build());
        }
        // Stop Button //
        Intent stopIntent = new Intent(this, this.getClass())
                .putExtra("stop", true);
        PendingIntent stopPending = PendingIntent.getService(this, 4, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT < 24)
            builder.addAction(new NotificationCompat.Action.Builder(R.drawable.icon_close, "", stopPending).build());
        else
            builder.addAction(new NotificationCompat.Action.Builder(R.drawable.icon_close, "Stop", stopPending).build());
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
        voiceOfKanacchi.setVolume(volume);
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