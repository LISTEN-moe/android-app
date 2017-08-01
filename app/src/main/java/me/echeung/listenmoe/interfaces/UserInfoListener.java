package me.echeung.listenmoe.interfaces;

import me.echeung.listenmoe.model.UserInfo;

public interface UserInfoListener {
    void onFailure(final String result);

    void onSuccess(final UserInfo userInfo);
}
