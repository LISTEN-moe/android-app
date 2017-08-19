package me.echeung.moemoekyun.api.old.interfaces;

import me.echeung.moemoekyun.api.old.model.UserInfo;

public interface UserInfoListener {
    void onFailure(final String result);
    void onSuccess(final UserInfo userInfo);
}
