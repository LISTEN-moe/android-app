package me.echeung.moemoekyun.api.interfaces;

import me.echeung.moemoekyun.api.responses.UserResponse;

public interface UserInfoListener {
    void onFailure(final String result);
    void onSuccess(final UserResponse userResponse);
}
