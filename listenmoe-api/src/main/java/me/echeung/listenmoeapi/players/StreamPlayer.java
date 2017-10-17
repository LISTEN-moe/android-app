package me.echeung.listenmoeapi.players;

public interface StreamPlayer {
    boolean isStarted();
    boolean isPlaying();

    boolean play();
    boolean pause();
    boolean stop();
    void fadeOut(Runnable callback);

    void duck();
    void unduck();
}
