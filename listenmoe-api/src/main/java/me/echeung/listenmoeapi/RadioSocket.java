package me.echeung.listenmoeapi;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.google.gson.Gson;

import me.echeung.listenmoeapi.auth.AuthUtil;
import me.echeung.listenmoeapi.responses.socket.SocketBaseResponse;
import me.echeung.listenmoeapi.responses.socket.SocketConnectResponse;
import me.echeung.listenmoeapi.responses.socket.SocketUpdateResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class RadioSocket extends WebSocketListener {

    private static final String TAG = RadioSocket.class.getSimpleName();

    private static final String SOCKET_URL = "wss://listen.moe/gateway";

    private static final Gson GSON = new Gson();

    private static final int RETRY_TIME_MIN = 250;
    private static final int RETRY_TIME_MAX = 4000;
    private int retryTime = RETRY_TIME_MIN;

    private final OkHttpClient client;
    private final AuthUtil authUtil;

    private volatile WebSocket socket;
    private SocketListener listener;

    private Handler heartbeatHandler;
    private Runnable heartbeatTask;

    RadioSocket(OkHttpClient client, AuthUtil authUtil) {
        this.client = client;
        this.authUtil = authUtil;

        heartbeatHandler = new Handler();
    }

    public void setListener(SocketListener listener) {
        this.listener = listener;
    }

    public void connect() {
        if (socket != null) {
            disconnect();
        }

        Log.d(TAG, "Connecting to socket...");
        final Request request = new Request.Builder().url(SOCKET_URL).build();
        socket = client.newWebSocket(request, this);

        clearHeartbeat();
    }

    public void reconnect() {
        Log.d(TAG, String.format("Reconnecting to socket in %d ms", retryTime));

        disconnect();

        // Exponential backoff
        SystemClock.sleep(retryTime);
        if (retryTime < RETRY_TIME_MAX) {
            retryTime *= 2;
        }

        connect();
    }

    public void disconnect() {
        Log.d(TAG, "Disconnected from socket");
        if (socket != null) {
            socket.cancel();
            socket = null;
        }

        clearHeartbeat();

        parseWebSocketResponse(null);
    }

    @Override
    public void onOpen(WebSocket socket, Response response) {
        Log.d(TAG, "Socket connection opened");

        retryTime = RETRY_TIME_MIN;
        clearHeartbeat();

        // Handshake with socket
        final String authToken = authUtil.isAuthenticated() ? authUtil.getAuthTokenWithPrefix() : "";
        socket.send(String.format("{ \"op\": 0, \"d\": { \"auth\": \"%s\" } }", authToken));
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        parseWebSocketResponse(text);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Log.e(TAG, t.getMessage(), t);
        reconnect();
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        Log.d(TAG, "Socket connection closed");
        reconnect();
    }

    // TODO: heartbeat handler on dead thread after sleep, reconnect on wake?
    private void heartbeat(int milliseconds) {
        clearHeartbeat();

        heartbeatTask = () -> {
            Log.d(TAG, "Sending heartbeat to socket");
            socket.send("{ \"op\": 9 }");

            // Repeat
            heartbeatHandler.postDelayed(heartbeatTask, milliseconds);
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
        Log.d(TAG, "Parsing socket data: " + jsonString);

        if (listener == null) return;

        if (jsonString == null) {
            listener.onSocketFailure();
            return;
        }

        // Heartbeat init
        final SocketBaseResponse baseResponse = GSON.fromJson(jsonString, SocketBaseResponse.class);
        if (baseResponse.getOp() == 0) {
            final SocketConnectResponse connectResponse = GSON.fromJson(jsonString, SocketConnectResponse.class);
            heartbeat(connectResponse.getD().getHeartbeat());
            return;
        }

        // Track update
        if (baseResponse.getOp() == 1) {
            final SocketUpdateResponse updateResponse = GSON.fromJson(jsonString, SocketUpdateResponse.class);
            if (!updateResponse.getT().equals("TRACK_UPDATE")) return;
            listener.onSocketReceive(updateResponse.getD());
        }
    }

    public interface SocketListener {
        void onSocketReceive(SocketUpdateResponse.Details info);
        void onSocketFailure();
    }

}
