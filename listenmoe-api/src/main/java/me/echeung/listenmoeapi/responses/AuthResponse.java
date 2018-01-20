package me.echeung.listenmoeapi.responses;

import lombok.Getter;

@Getter
public class AuthResponse extends BaseResponse {
    private String token;   // JWT token; only valid for ~2m if MFA is required
    private String apiKey;  // Only if requested via developer access
    private boolean mfa;
}
