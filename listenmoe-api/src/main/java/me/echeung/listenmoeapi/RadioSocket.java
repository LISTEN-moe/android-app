package me.echeung.listenmoeapi;

import android.os.SystemClock;
import android.util.Log;

import com.google.gson.Gson;

import me.echeung.listenmoeapi.models.PlaybackInfo;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class RadioSocket extends WebSocketListener {

    private static final String TAG = RadioSocket.class.getSimpleName();

    private static final String SOCKET_URL = "wss://listen.moe/api/v2/socket";

    private static final int RETRY_TIME_MIN = 250;
    private static final int RETRY_TIME_MAX = 4000;
    private int retryTime = RETRY_TIME_MIN;

    private static final Gson GSON = new Gson();

    private final OkHttpClient client;
    private final APIClient.APIHelper apiHelper;

    private WebSocket socket;
    private SocketListener listener;

    RadioSocket(OkHttpClient client, APIClient.APIHelper apiClient) {
        this.client = client;
        this.apiHelper = apiClient;
    }

    public void setListener(SocketListener listener) {
        this.listener = listener;
    }

    public void connect() {
        final Request request = new Request.Builder().url(SOCKET_URL).build();
        socket = client.newWebSocket(request, this);
    }

    public void disconnect() {
        if (socket != null) {
            socket.cancel();
            socket = null;
        }

        parseWebSocketResponse(null);
    }

    public void update() {
        final String authToken = apiHelper.getAuthToken();
        if (authToken == null || authToken.isEmpty()) {
            return;
        }

        if (socket == null) {
            connect();
        }

        socket.send("{\"token\":\"" + authToken + "\"}");
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
        Log.e(TAG, t.getMessage(), t);
        reconnect();
    }

    @Override
    public void	onClosed(WebSocket webSocket, int code, String reason) {
        reconnect();
    }

    private void reconnect() {
        disconnect();

        // Exponential backoff
        SystemClock.sleep(retryTime);
        if (retryTime < RETRY_TIME_MAX) {
            retryTime *= 2;
        }

        connect();
    }

    private void parseWebSocketResponse(final String jsonString) {
        if (listener == null) return;

        if (jsonString == null) {
            listener.onSocketFailure();
            return;
        }

        final PlaybackInfo playbackInfo = GSON.fromJson(jsonString, PlaybackInfo.class);
        listener.onSocketReceive(playbackInfo);
    }

    public interface SocketListener {
        void onSocketReceive(PlaybackInfo info);
        void onSocketFailure();
    }
}