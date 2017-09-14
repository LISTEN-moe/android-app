package me.echeung.listenmoeapi.callbacks;

import me.echeung.listenmoeapi.responses.UserResponse;

public interface UserInfoCallback extends BaseCallback {
    void onSuccess(final UserResponse userResponse);
}
