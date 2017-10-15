package me.echeung.listenmoeapi;

import android.content.Context;

import me.echeung.listenmoeapi.players.AndroidPlayer;
import me.echeung.listenmoeapi.players.StreamPlayer;

public class RadioStream {

    // Vorbis: /stream, Opus: /opus, mp3: /fallback
    private static final String STREAM_URL = "https://listen.moe/fallback";

    private StreamPlayer player;

    RadioStream(Context context) {
        this.player = new AndroidPlayer(context, STREAM_URL);
    }

    public boolean isStarted() {
        return player.isStarted();
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public boolean play() {
        return player.play();
    }

    public boolean pause() {
        return player.pause();
    }

    public boolean stop() {
        return player.stop();
    }

    public void stop(Runnable callback) {
        player.stop(callback);
    }

    public void duck() {
        player.duck();
    }

    public void unduck() {
        player.unduck();
    }
}
