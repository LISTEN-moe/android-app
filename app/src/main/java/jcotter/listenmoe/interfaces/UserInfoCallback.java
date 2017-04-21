package jcotter.listenmoe.interfaces;

import jcotter.listenmoe.model.UserInfo;

public interface UserInfoCallback {
    void onFailure(final String result);

    void onSuccess(final UserInfo userInfo);
}
