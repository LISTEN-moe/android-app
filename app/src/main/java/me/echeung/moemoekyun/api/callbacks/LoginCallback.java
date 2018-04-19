package me.echeung.moemoekyun.api.callbacks;

public interface LoginCallback extends BaseCallback {
    void onSuccess(final String token);
    void onMfaRequired(final String token);
}
