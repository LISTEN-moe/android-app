package me.echeung.listenmoeapi;

import android.content.Context;
import android.net.wifi.WifiManager;

import me.echeung.listenmoeapi.players.AndroidPlayer;
import me.echeung.listenmoeapi.players.StreamPlayer;

public class RadioStream {

    // Vorbis: /stream, Opus: /opus, mp3: /fallback
    private static final String STREAM_URL = "https://listen.moe/fallback";

    private static final String WIFI_LOCK_TAG = "listenmoe_wifi_lock";

    private StreamPlayer player;
    private final WifiManager.WifiLock wifiLock;

    RadioStream(Context context) {
        this.player = new AndroidPlayer(context, STREAM_URL);

        this.wifiLock =
                ((WifiManager) context.getApplicationContext()
                        .getSystemService(Context.WIFI_SERVICE))
                        .createWifiLock(WifiManager.WIFI_MODE_FULL, WIFI_LOCK_TAG);
    }

    public boolean isStarted() {
        return player.isStarted();
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public boolean play() {
        if (player.play()) {
            acquireWifiLock();
            return true;
        }

        return false;
    }

    public boolean pause() {
        if (player.pause()) {
            releaseWifiLock();
            return true;
        }

        return false;
    }

    public boolean stop() {
        if (player.stop()) {
            releaseWifiLock();
            return true;
        }

        return false;
    }

    public void duck() {
        player.duck();
    }

    public void unduck() {
        player.unduck();
    }

    private void acquireWifiLock() {
        if (wifiLock != null) {
            releaseWifiLock();
            wifiLock.acquire();
        }
    }

    private void releaseWifiLock() {
        if (wifiLock != null && wifiLock.isHeld()) {
            wifiLock.release();
        }
    }
}
