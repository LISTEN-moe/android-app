package jcotter.listenmoe.interfaces;

import jcotter.listenmoe.model.UserInfo;

public interface UserInfoListener {
    void onFailure(final String result);

    void onSuccess(final UserInfo userInfo);
}
