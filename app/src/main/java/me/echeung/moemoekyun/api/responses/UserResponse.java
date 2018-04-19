package me.echeung.moemoekyun.api.responses;

import lombok.Getter;
import me.echeung.moemoekyun.models.User;

@Getter
public class UserResponse extends BaseResponse {
    private User user;
}
