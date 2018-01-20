package me.echeung.listenmoeapi.responses;

import lombok.Getter;

@Getter
public class AuthResponse extends BaseResponse {
    private String token;
}
