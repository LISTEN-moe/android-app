package me.echeung.moemoekyun.interfaces;

public interface AuthListener {
    void onFailure(final String result);

    void onSuccess(final String result);
}
