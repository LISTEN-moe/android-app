package me.echeung.moemoekyun.api.responses;

import lombok.Getter;
import me.echeung.moemoekyun.api.models.User;

@Getter
public class UserResponse extends BaseResponse {
    private User user;
}
