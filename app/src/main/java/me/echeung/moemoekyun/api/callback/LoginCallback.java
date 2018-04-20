package me.echeung.moemoekyun.api.callback;

public interface LoginCallback extends BaseCallback {
    void onSuccess( String token);
    void onMfaRequired( String token);
}
