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

    private static final String SOCKET_URL = "wss://dev.listen.moe/gateway";

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

    public synchronized void connect() {
        final Request request = new Request.Builder().url(SOCKET_URL).build();
        socket = client.newWebSocket(request, this);

        clearHeartbeat();
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

    public synchronized void disconnect() {
        if (socket != null) {
            socket.cancel();
            socket = null;
        }

        clearHeartbeat();

        parseWebSocketResponse(null);
    }

    @Override
    public void onOpen(WebSocket socket, Response response) {
        retryTime = RETRY_TIME_MIN;

        clearHeartbeat();

        // Authenticate with socket
        String authToken = authUtil.getAuthToken();
        authToken = authToken == null || authToken.isEmpty() ? "" : "Bearer " + authToken;
        socket.send("{ \"op\": 0, \"d\": { \"auth\": \"Bearer " + authToken + "\" } }");
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
    public void	onClosed(WebSocket webSocket, int code, String reason) {
        reconnect();
    }

    private void heartbeat(int milliseconds) {
        heartbeatTask = () -> socket.send("{ \"op\": 9 }");
        heartbeatHandler.postDelayed(heartbeatTask, milliseconds);
    }

    private void clearHeartbeat() {
        if (heartbeatTask != null) {
            heartbeatHandler.removeCallbacks(heartbeatTask);
            heartbeatTask = null;
        }
    }

    private void parseWebSocketResponse(final String jsonString) {
        if (listener == null) return;

        if (jsonString == null) {
            listener.onSocketFailure();
            return;
        }

        final SocketBaseResponse baseResponse = GSON.fromJson(jsonString, SocketBaseResponse.class);
        if (baseResponse.getOp() == 0) {
            final SocketConnectResponse connectResponse = GSON.fromJson(jsonString, SocketConnectResponse.class);
            heartbeat(connectResponse.getD().getHeartbeat());
            return;
        }

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
