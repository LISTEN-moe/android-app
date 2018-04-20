package me.echeung.moemoekyun.client.api.callback;

public interface LoginCallback extends BaseCallback {
    void onSuccess( String token);
    void onMfaRequired( String token);
}
