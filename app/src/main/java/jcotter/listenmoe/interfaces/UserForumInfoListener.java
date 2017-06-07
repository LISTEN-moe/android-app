package jcotter.listenmoe.interfaces;

public interface UserForumInfoListener {
    void onFailure(final String result);

    void onSuccess(final String avatarUrl);
}
