package jcotter.listenmoe.interfaces;

public interface RequestSongListener {
    void onFailure(final String result);

    void onSuccess();
}
