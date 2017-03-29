package jcotter.listenmoe.interfaces;

public interface UserInfoCallback {
    void onFailure(final String result);

    void onSuccess(final String jsonResult);
}
