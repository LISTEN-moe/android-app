package me.echeung.listenmoeapi.callbacks;

public interface AuthCallback extends BaseCallback {
    void onSuccess(final String token);
    void onMfaRequired(final String token);
}
