package jcotter.listenmoe.interfaces;

public interface FavoriteSongCallback {
    void onFailure(final String result);

    void onSuccess(final String jsonResult);
}
