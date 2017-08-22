package me.echeung.moemoekyun.api.v3.interfaces;

public interface AuthListener {
    void onFailure(final String result);
    void onSuccess(final String result);
}
