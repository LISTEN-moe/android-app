package me.echeung.moemoekyun.api.callbacks;

public interface LoginCallback extends BaseCallback {
    void onSuccess( String token);
    void onMfaRequired( String token);
}
