package me.echeung.moemoekyun.api.v3.interfaces;

public interface UserForumInfoListener {
    void onFailure(final String result);
    void onSuccess(final String avatarUrl);
}
