package jcotter.listenmoe.interfaces;

import jcotter.listenmoe.model.UserForumInfo;

public interface UserForumInfoCallback {
    void onFailure(final String result);

    void onSuccess(final String avatarUrl);
}
