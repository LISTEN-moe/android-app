package me.echeung.listenmoeapi;

import android.content.Context;

import me.echeung.listenmoeapi.players.AndroidPlayer;
import me.echeung.listenmoeapi.players.StreamPlayer;

public class RadioStream {

    // Vorbis: /stream, Opus: /opus, mp3: /fallback
    private static final String STREAM_URL = "https://listen.moe/fallback";

    private StreamPlayer player;
    private Callback callback;

    RadioStream(Context context) {
        this.player = new AndroidPlayer(context, STREAM_URL);
    }

    public void setListener(Callback callback) {
        this.callback = callback;
    }

    public void removeListener() {
        this.callback = null;
    }

    public boolean isStarted() {
        return player.isStarted();
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void play() {
        if (player.play() && callback != null) {
            callback.onPlay();
        }
    }

    public void pause() {
        if (player.pause() && callback != null) {
            callback.onPause();
        }
    }

    public void stop() {
        if (player.stop() && callback != null) {
            callback.onStop();
        }
    }

    public void fadeOut() {
        player.fadeOut(() -> {
            if (callback != null) {
                callback.onStop();
            }
        });
    }

    public void duck() {
        player.duck();
    }

    public void unduck() {
        player.unduck();
    }

    public interface Callback {
        void onPlay();
        void onPause();
        void onStop();
    }
}
