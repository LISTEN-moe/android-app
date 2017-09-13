package me.echeung.listenmoeapi.interfaces;

public interface RequestSongListener {
    void onFailure(final String result);
    void onSuccess();
}
