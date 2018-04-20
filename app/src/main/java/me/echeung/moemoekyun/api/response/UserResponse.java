package me.echeung.moemoekyun.api.response;

import lombok.Getter;
import me.echeung.moemoekyun.model.User;

@Getter
public class UserResponse extends BaseResponse {
    private User user;
}
