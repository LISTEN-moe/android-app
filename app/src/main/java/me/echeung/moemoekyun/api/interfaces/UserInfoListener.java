package me.echeung.moemoekyun.api.interfaces;

import me.echeung.moemoekyun.api.models.UserInfo;

public interface UserInfoListener {
    void onFailure(final String result);
    void onSuccess(final UserInfo userInfo);
}
