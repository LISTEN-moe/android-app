package me.echeung.moemoekyun.api.callbacks;

import me.echeung.moemoekyun.api.models.User;

public interface UserInfoCallback extends BaseCallback {
    void onSuccess(final User user);
}
