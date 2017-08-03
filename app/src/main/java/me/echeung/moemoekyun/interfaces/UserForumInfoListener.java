package me.echeung.moemoekyun.interfaces;

public interface UserForumInfoListener {
    void onFailure(final String result);

    void onSuccess(final String avatarUrl);
}
