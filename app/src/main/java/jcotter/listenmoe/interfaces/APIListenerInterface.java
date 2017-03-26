package jcotter.listenmoe.interfaces;

public interface APIListenerInterface {
    void favoriteListCallback(String jsonResult);

    void authenticateCallback(String token);

    void requestCallback(String jsonResult);

    void favoriteCallback(String jsonResult);

    void searchCallback(String jsonResult);
}
