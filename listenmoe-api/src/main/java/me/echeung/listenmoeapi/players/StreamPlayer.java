package me.echeung.listenmoeapi.players;

public interface StreamPlayer {
    boolean isStarted();
    boolean isPlaying();

    boolean play();
    boolean pause();
    boolean stop();
    void stop(Runnable callback);

    void duck();
    void unduck();
}
