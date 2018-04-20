package me.echeung.moemoekyun.client.api.response;

import lombok.Getter;
import me.echeung.moemoekyun.client.model.User;

@Getter
public class UserResponse extends BaseResponse {
    private User user;
}
