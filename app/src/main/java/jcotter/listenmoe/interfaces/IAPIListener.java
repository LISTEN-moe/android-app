package jcotter.listenmoe.interfaces;

public interface IAPIListener {
    void favoriteListCallback(String jsonResult);

    void authenticateCallback(String token);

    void requestCallback(String jsonResult);

    void favoriteCallback(String jsonResult);

    void searchCallback(String jsonResult);
}
