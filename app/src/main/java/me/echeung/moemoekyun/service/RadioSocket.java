package me.echeung.moemoekyun.service;

import android.os.SystemClock;

import com.google.gson.Gson;

import me.echeung.moemoekyun.api.v3.model.PlaybackInfo;
import me.echeung.moemoekyun.api.v3.model.Song;
import me.echeung.moemoekyun.constants.Endpoints;
import me.echeung.moemoekyun.utils.AuthUtil;
import me.echeung.moemoekyun.utils.NetworkUtil;
import me.echeung.moemoekyun.viewmodels.AppState;
import me.echeung.moemoekyun.viewmodels.UserState;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class RadioSocket  extends WebSocketListener {

    private static final Gson GSON = new Gson();

    private static final int RETRY_TIME_MIN = 250;
    private static final int RETRY_TIME_MAX = 4000;

    private RadioService service;

    private WebSocket socket;
    private int retryTime = RETRY_TIME_MIN;

    public RadioSocket(RadioService service) {
        this.service = service;
    }

    public void connect() {
        if (!NetworkUtil.isNetworkAvailable(service)) {
            return;
        }

        final Request request = new Request.Builder().url(Endpoints.SOCKET).build();
        socket = new OkHttpClient().newWebSocket(request, this);
    }

    public void disconnect() {
        if (socket != null) {
            socket.cancel();
            socket = null;
        }
        parseWebSocketResponse(null);
    }

    public void reconnect() {
        disconnect();

        // Exponential backoff
        SystemClock.sleep(retryTime);
        if (retryTime < RETRY_TIME_MAX) {
            retryTime *= 2;
        }

        connect();
    }

    public void update() {
        if (AuthUtil.isAuthenticated(service)) {
            final String authToken = AuthUtil.getAuthToken(service);
            if (authToken != null) {
                if (socket == null) {
                    connect();
                }

                socket.send("{\"token\":\"" + authToken + "\"}");
            }
        }
    }

    @Override
    public void onOpen(WebSocket socket, Response response) {
        retryTime = RETRY_TIME_MIN;
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

                service.sendPublicIntent(RadioService.META_CHANGED);
            }

            state.listeners.set(playbackInfo.getListeners());
            state.requester.set(playbackInfo.getRequestedBy());
        }

        service.updateNotification();
    }
}