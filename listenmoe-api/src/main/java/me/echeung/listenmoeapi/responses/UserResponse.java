package me.echeung.listenmoeapi.responses;

import lombok.Getter;
import me.echeung.listenmoeapi.models.User;

@Getter
public class UserResponse extends BaseResponse {
    private User user;
}
