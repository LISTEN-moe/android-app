package me.echeung.listenmoeapi.radio;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.google.gson.Gson;

import me.echeung.listenmoeapi.auth.AuthUtil;
import me.echeung.listenmoeapi.endpoints.Endpoints;
import me.echeung.listenmoeapi.responses.socket.SocketBaseResponse;
import me.echeung.listenmoeapi.responses.socket.SocketConnectResponse;
import me.echeung.listenmoeapi.responses.socket.SocketUpdateResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class Socket extends WebSocketListener {

    private static final String TAG = Socket.class.getSimpleName();

    private static final Gson GSON = new Gson();

    private static final String TRACK_UPDATE = "TRACK_UPDATE";
    private static final String TRACK_UPDATE_REQUEST = "TRACK_UPDATE_REQUEST";

    private static final int RETRY_TIME_MIN = 250;
    private static final int RETRY_TIME_MAX = 4000;
    private int retryTime = RETRY_TIME_MIN;
    private boolean attemptingReconnect = false;

    private final OkHttpClient client;
    private final AuthUtil authUtil;

    private WebSocket socket;
    private Listener listener;

    private Handler heartbeatHandler;
    private Runnable heartbeatTask;

    public Socket(OkHttpClient client, AuthUtil authUtil) {
        this.client = client;
        this.authUtil = authUtil;

        heartbeatHandler = new Handler();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void connect() {
        Log.d(TAG, "Connecting to socket...");

        if (socket != null) {
            disconnect();
        }

        final Request request = new Request.Builder().url(Endpoints.SOCKET).build();
        socket = client.newWebSocket(request, this);
    }

    public void disconnect() {
        clearHeartbeat();

        if (socket != null) {
            socket.cancel();
            socket = null;
        }

        Log.d(TAG, "Disconnected from socket");
    }

    public void reconnect() {
        if (attemptingReconnect) return;

        Log.d(TAG, String.format("Reconnecting to socket in %d ms", retryTime));

        disconnect();

        attemptingReconnect = true;

        // Exponential backoff
        SystemClock.sleep(retryTime);
        if (retryTime < RETRY_TIME_MAX) {
            retryTime *= 2;
        }

        connect();
    }

    public void update() {
        Log.d(TAG, "Requesting update from socket");

        if (socket == null) {
            connect();
            return;
        }

        socket.send("{ \"op\": 2 }");
    }

    @Override
    public void onOpen(WebSocket socket, Response response) {
        Log.d(TAG, "Socket connection opened");

        retryTime = RETRY_TIME_MIN;
        attemptingReconnect = false;

        // Handshake with socket
        final String authToken = authUtil.isAuthenticated() ? authUtil.getAuthTokenWithPrefix() : "";
        socket.send(String.format("{ \"op\": 0, \"d\": { \"auth\": \"%s\" } }", authToken));
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Log.d(TAG, "Received message from socket: " + text);

        parseWebSocketResponse(text);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Log.e(TAG, "Socket failure: " + t.getMessage(), t);
        reconnect();
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        Log.d(TAG, "Socket connection closed: " + reason);
        reconnect();
    }

    private void heartbeat(int milliseconds) {
        clearHeartbeat();

        heartbeatTask = new Runnable() {
            @Override
            public void run() {
                if (socket == null) return;

                Log.d(TAG, "Sending heartbeat to socket");
                socket.send("{ \"op\": 9 }");

                // Repeat
                heartbeatHandler.postDelayed(this, milliseconds);
            }
        };

        heartbeatHandler.postDelayed(heartbeatTask, milliseconds);
        Log.d(TAG, String.format("Created heartbeat task for %d ms", milliseconds));
    }

    private void clearHeartbeat() {
        if (heartbeatTask != null) {
            Log.d(TAG, "Removing heartbeat task");
            heartbeatHandler.removeCallbacksAndMessages(null);
            heartbeatTask = null;
        }
    }

    private void parseWebSocketResponse(final String jsonString) {
        if (listener == null) {
            Log.d(TAG, "Listener is null");
            return;
        }

        if (jsonString == null) {
            listener.onSocketFailure();
            return;
        }

        final SocketBaseResponse baseResponse = GSON.fromJson(jsonString, SocketBaseResponse.class);
        switch (baseResponse.getOp()) {
            // Heartbeat init
            case 0:
                final SocketConnectResponse connectResponse = GSON.fromJson(jsonString, SocketConnectResponse.class);
                heartbeat(connectResponse.getD().getHeartbeat());
                break;

            // Track update
            case 1:
                final SocketUpdateResponse updateResponse = GSON.fromJson(jsonString, SocketUpdateResponse.class);
                if (!updateResponse.getT().equals(TRACK_UPDATE) && !updateResponse.getT().equals(TRACK_UPDATE_REQUEST)) return;
                listener.onSocketReceive(updateResponse.getD());
                break;

            // Heartbeat ACK
            case 10:
                break;

            default:
                Log.d(TAG, "Received invalid socket data: " + jsonString);
        }
    }

    public interface Listener {
        void onSocketReceive(SocketUpdateResponse.Details info);
        void onSocketFailure();
    }

}
