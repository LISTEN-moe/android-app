package me.echeung.moemoekyun.api.interfaces;

public interface AuthListener {
    void onFailure(final String result);
    void onSuccess(final String result);
}
