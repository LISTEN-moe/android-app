package me.echeung.listenmoeapi.responses;

import lombok.Getter;

@Getter
public class BaseResponse {
    private boolean success;
    private String message;
}
