package me.echeung.moemoekyun.api.v3.interfaces;

import me.echeung.moemoekyun.api.v3.model.UserInfo;

public interface UserInfoListener {
    void onFailure(final String result);
    void onSuccess(final UserInfo userInfo);
}
