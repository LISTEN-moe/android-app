package me.echeung.moemoekyun.api.callback;

import me.echeung.moemoekyun.model.User;

public interface UserInfoCallback extends BaseCallback {
    void onSuccess(User user);
}
