package me.echeung.moemoekyun.api.old.interfaces;

public interface AuthListener {
    void onFailure(final String result);
    void onSuccess(final String result);
}
