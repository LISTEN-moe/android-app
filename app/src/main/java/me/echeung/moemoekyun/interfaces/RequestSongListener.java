package me.echeung.moemoekyun.interfaces;

public interface RequestSongListener {
    void onFailure(final String result);
    void onSuccess();
}
