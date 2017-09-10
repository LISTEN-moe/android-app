package me.echeung.moemoekyun.service;

import android.os.SystemClock;
import android.util.Log;

import com.google.gson.Gson;

import me.echeung.moemoekyun.api.models.PlaybackInfo;
import me.echeung.moemoekyun.api.models.Song;
import me.echeung.moemoekyun.constants.Endpoints;
import me.echeung.moemoekyun.ui.App;
import me.echeung.moemoekyun.utils.AuthUtil;
import me.echeung.moemoekyun.utils.NetworkUtil;
import me.echeung.moemoekyun.viewmodels.RadioViewModel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class RadioSocket extends WebSocketListener {

    private static final String TAG = RadioSocket.class.getSimpleName();

    private static final Gson GSON = new Gson();

    private static final int RETRY_TIME_MIN = 250;
    private static final int RETRY_TIME_MAX = 4000;

    private RadioService service;

    private WebSocket socket;
    private int retryTime = RETRY_TIME_MIN;

    RadioSocket(RadioService service) {
        this.service = service;
    }

    void connect() {
        if (!NetworkUtil.isNetworkAvailable(service)) {
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

    void update() {
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
        final RadioViewModel viewModel = App.getRadioViewModel();

        if (jsonString == null) {
            viewModel.reset();
        } else {
            final PlaybackInfo playbackInfo = GSON.fromJson(jsonString, PlaybackInfo.class);

            if (playbackInfo.getSongId() != 0) {
                viewModel.setCurrentSong(new Song(
                        playbackInfo.getSongId(),
                        playbackInfo.getArtistName().trim(),
                        playbackInfo.getSongName().trim(),
                        playbackInfo.getAnimeName().trim()
                ));

                viewModel.setLastSong(playbackInfo.getLast().toString());
                viewModel.setSecondLastSong(playbackInfo.getSecondLast().toString());

                if (playbackInfo.hasExtended()) {
                    final PlaybackInfo.ExtendedInfo extended = playbackInfo.getExtended();

                    viewModel.setIsFavorited(extended.isFavorite());

                    App.getUserViewModel().setQueueSize(extended.getQueue().getSongsInQueue());
                    App.getUserViewModel().setQueuePosition(extended.getQueue().getInQueueBeforeUserSong());
                }

                service.sendPublicIntent(RadioService.META_CHANGED);
            }

            viewModel.setListeners(playbackInfo.getListeners());
            viewModel.setRequester(playbackInfo.getRequestedBy());
        }

        service.updateNotification();
    }
}