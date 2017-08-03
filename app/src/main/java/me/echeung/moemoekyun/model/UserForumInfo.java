package me.echeung.moemoekyun.model;

public class UserForumInfo {
    private UserForumInfoData data;

    public UserForumInfo(UserForumInfoData data) {
        this.data = data;
    }

    public String getAvatarUrl() {
        return data.attributes.avatarUrl;
    }

    private class UserForumInfoData {
        UserForumInfoDataAttributes attributes;
    }

    private class UserForumInfoDataAttributes {
        String avatarUrl;
    }
}
