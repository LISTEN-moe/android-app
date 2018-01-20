package me.echeung.listenmoeapi.responses;

import lombok.Getter;

@Getter
public class UserResponse extends BaseResponse {
    private int id;
    private String username;
}
