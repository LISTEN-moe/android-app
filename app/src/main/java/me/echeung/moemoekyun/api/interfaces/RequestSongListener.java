package me.echeung.moemoekyun.api.interfaces;

public interface RequestSongListener {
    void onFailure(final String result);
    void onSuccess();
}
