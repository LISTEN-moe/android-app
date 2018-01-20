package me.echeung.listenmoeapi.callbacks;

import me.echeung.listenmoeapi.models.User;

public interface UserInfoCallback extends BaseCallback {
    void onSuccess(final User user);
}
