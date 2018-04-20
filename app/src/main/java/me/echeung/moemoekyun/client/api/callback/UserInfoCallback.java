package me.echeung.moemoekyun.client.api.callback;

import me.echeung.moemoekyun.client.model.User;

public interface UserInfoCallback extends BaseCallback {
    void onSuccess(User user);
}
