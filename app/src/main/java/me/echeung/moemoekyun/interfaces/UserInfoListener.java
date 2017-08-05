package me.echeung.moemoekyun.interfaces;

import me.echeung.moemoekyun.model.UserInfo;

public interface UserInfoListener {
    void onFailure(final String result);
    void onSuccess(final UserInfo userInfo);
}
