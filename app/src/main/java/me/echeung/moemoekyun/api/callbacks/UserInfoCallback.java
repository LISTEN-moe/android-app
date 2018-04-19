package me.echeung.moemoekyun.api.callbacks;

import me.echeung.moemoekyun.models.User;

public interface UserInfoCallback extends BaseCallback {
    void onSuccess(User user);
}
