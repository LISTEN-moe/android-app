package me.echeung.listenmoeapi.interfaces;

import me.echeung.listenmoeapi.responses.UserResponse;

public interface UserInfoListener {
    void onFailure(final String result);
    void onSuccess(final UserResponse userResponse);
}
