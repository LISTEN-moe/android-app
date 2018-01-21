package me.echeung.listenmoeapi;

import android.content.Context;

import me.echeung.listenmoeapi.player.StreamPlayer;

public class RadioStream {

    private static final String STREAM_VORBIS = "https://listen.moe/stream";
    private static final String STREAM_OPUS = "https://listen.moe/opus";
    private static final String STREAM_MP3 = "https://listen.moe/fallback";

    private StreamPlayer player;
    private Callback callback;

    RadioStream(Context context) {
        this.player = new StreamPlayer(context, STREAM_MP3);
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
