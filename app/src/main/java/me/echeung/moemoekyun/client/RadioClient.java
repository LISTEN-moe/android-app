package me.echeung.moemoekyun.client;

import android.content.Context;

import lombok.Getter;
import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.client.api.APIClient;
import me.echeung.moemoekyun.client.api.library.Jpop;
import me.echeung.moemoekyun.client.api.library.Kpop;
import me.echeung.moemoekyun.client.api.library.Library;
import me.echeung.moemoekyun.client.auth.AuthUtil;
import me.echeung.moemoekyun.client.network.NetworkClient;
import me.echeung.moemoekyun.client.socket.Socket;
import me.echeung.moemoekyun.client.stream.Stream;
import okhttp3.OkHttpClient;

@Getter
public class RadioClient {

    private final APIClient api;
    private final Socket socket;
    private final Stream stream;
    private final AuthUtil authUtil;

    @Getter
    private static Library library;

    public RadioClient(Context context) {
        setLibrary(App.getPreferenceUtil().getLibraryMode());

        OkHttpClient okHttpClient = NetworkClient.getClient();

        this.authUtil = new AuthUtil(context);
        this.api = new APIClient(okHttpClient, authUtil);
        this.socket = new Socket(okHttpClient, authUtil);
        this.stream = new Stream(context);
    }

    public void changeLibrary(String newMode) {
        setLibrary(newMode);

        socket.reconnect();

        final boolean wasPlaying = stream.isPlaying();
        stream.stop();
        if (wasPlaying) {
            stream.play();
        }
    }

    private void setLibrary(String libraryName) {
        RadioClient.library = libraryName.equals(Kpop.NAME) ? Kpop.INSTANCE : Jpop.INSTANCE;
    }

}
