package me.echeung.listenmoeapi.interfaces;

public interface AuthListener {
    void onFailure(final String result);
    void onSuccess(final String result);
}
