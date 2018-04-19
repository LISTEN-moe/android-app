package me.echeung.moemoekyun.api.callbacks;

import me.echeung.moemoekyun.model.User;

public interface UserInfoCallback extends BaseCallback {
    void onSuccess(User user);
}
