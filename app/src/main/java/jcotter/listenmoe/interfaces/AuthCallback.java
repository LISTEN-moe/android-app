package jcotter.listenmoe.interfaces;

public interface AuthCallback {
    void onFailure(final String result);

    void onSuccess(final String result);
}
