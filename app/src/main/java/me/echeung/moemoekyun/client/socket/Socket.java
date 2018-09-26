package me.echeung.moemoekyun.client.socket;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.squareup.moshi.Moshi;

import java.io.IOException;

import me.echeung.moemoekyun.client.RadioClient;
import me.echeung.moemoekyun.client.auth.AuthUtil;
import me.echeung.moemoekyun.client.socket.response.BaseResponse;
import me.echeung.moemoekyun.client.socket.response.ConnectResponse;
import me.echeung.moemoekyun.client.socket.response.EventNotificationResponse;
import me.echeung.moemoekyun.client.socket.response.NotificationResponse;
import me.echeung.moemoekyun.client.socket.response.UpdateResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class Socket extends WebSocketListener {

    private static final String TAG = Socket.class.getSimpleName();

    private static final Moshi MOSHI = new Moshi.Builder().build();

    private static final String TRACK_UPDATE = "TRACK_UPDATE";
    private static final String TRACK_UPDATE_REQUEST = "TRACK_UPDATE_REQUEST";
    private static final String QUEUE_UPDATE = "QUEUE_UPDATE";
    private static final String NOTIFICATION = "NOTIFICATION";

    private static final int RETRY_TIME_MIN = 250;
    private static final int RETRY_TIME_MAX = 4000;
    private int retryTime = RETRY_TIME_MIN;
    private boolean attemptingReconnect = false;

    private final OkHttpClient client;
    private final AuthUtil authUtil;

    private volatile WebSocket socket;
    private final Object socketLock = new Object();

    private Listener listener;

    private Handler heartbeatHandler = new Handler();
    private Runnable heartbeatTask;

    public Socket(OkHttpClient client, AuthUtil authUtil) {
        this.client = client;
        this.authUtil = authUtil;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void connect() {
        synchronized (socketLock) {
            Log.d(TAG, "Connecting to socket...");

            if (socket != null) {
                disconnect();
            }

            final Request request = new Request.Builder().url(RadioClient.Companion.getLibrary().getSocketUrl()).build();
            socket = client.newWebSocket(request, this);
        }
    }

    public void disconnect() {
        synchronized (socketLock) {
            clearHeartbeat();

            if (socket != null) {
                socket.cancel();
                socket = null;
            }

            Log.d(TAG, "Disconnected from socket");
        }
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
        synchronized (socketLock) {
            Log.d(TAG, "Requesting update from socket");

            if (socket == null) {
                connect();
                return;
            }

            socket.send("{ \"op\": 2 }");
        }
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
                synchronized (socketLock) {
                    if (socket == null) return;

                    Log.d(TAG, "Sending heartbeat to socket");
                    socket.send("{ \"op\": 9 }");

                    // Repeat
                    heartbeatHandler.postDelayed(this, milliseconds);
                }
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

    private void parseWebSocketResponse(String jsonString) {
        if (listener == null) {
            Log.d(TAG, "Listener is null");
            return;
        }

        if (jsonString == null) {
            listener.onSocketFailure();
            return;
        }

        try {
            BaseResponse baseResponse = getResponse(BaseResponse.class, jsonString);
            switch (baseResponse.getOp()) {
                // Heartbeat init
                case 0:
                    ConnectResponse connectResponse = getResponse(ConnectResponse.class, jsonString);
                    heartbeat(connectResponse.getD().getHeartbeat());
                    break;

                // Update
                case 1:
                    UpdateResponse updateResponse = getResponse(UpdateResponse.class, jsonString);
                    if (!isValidUpdate(updateResponse)) {
                        return;
                    }
                    if (isNotification(updateResponse)) {
                        parseNotification(jsonString);
                        return;
                    }
                    listener.onSocketReceive(updateResponse.getD());
                    break;

                // Heartbeat ACK
                case 10:
                    break;

                default:
                    Log.d(TAG, "Received invalid socket data: " + jsonString);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to parse socket data: " + jsonString, e);
        }
    }

    private void parseNotification(String jsonString) {
        try {
            NotificationResponse notificationResponse = getResponse(NotificationResponse.class, jsonString);
            switch (notificationResponse.getT()) {
                case EventNotificationResponse.TYPE:
                    EventNotificationResponse eventResponse = getResponse(EventNotificationResponse.class, jsonString);
                    // TODO: do something with eventResponse
                    Log.i(TAG, "Got event: " + eventResponse.getD().getEvent().getName());
                    break;
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to parse notification data: " + jsonString, e);
        }
    }

    private boolean isValidUpdate(UpdateResponse updateResponse) {
        return updateResponse.getT().equals(TRACK_UPDATE)
                || updateResponse.getT().equals(TRACK_UPDATE_REQUEST)
                || updateResponse.getT().equals(QUEUE_UPDATE)
                || isNotification(updateResponse);
    }

    private boolean isNotification(UpdateResponse updateResponse) {
        return updateResponse.getT().equals(NOTIFICATION);
    }

    private <T> T getResponse(Class<T> responseClass, String jsonString) throws IOException {
        return MOSHI.adapter(responseClass).fromJson(jsonString);
    }

    public interface Listener {
        void onSocketReceive(UpdateResponse.Details info);

        void onSocketFailure();
    }

}
