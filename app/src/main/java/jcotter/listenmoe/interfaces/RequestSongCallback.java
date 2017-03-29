package jcotter.listenmoe.interfaces;

public interface RequestSongCallback {
    void onFailure(final String result);

    void onSuccess(final String jsonResult);
}
