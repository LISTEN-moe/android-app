package jcotter.listenmoe.interfaces;

public interface UserForumInfoCallback {
    void onFailure(final String result);

    void onSuccess(final String avatarUrl);
}
