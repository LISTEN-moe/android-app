package me.echeung.moemoekyun.api.v3.interfaces;

public interface RequestSongListener {
    void onFailure(final String result);
    void onSuccess();
}
