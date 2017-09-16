package me.echeung.listenmoeapi.responses;

import android.content.Context;

import me.echeung.listenmoeapi.R;

public class Messages {
    public static final String AUTH_ERROR = "error-login";
    public static final String AUTH_FAILURE = "Failed to authenticate token.";
    public static final String USER_NOT_SUPPORTER = "user-is-not-supporter";

    private static final String INVALID_PASS = "invalid-password";
    private static final String INVALID_USER = "invalid-user";
    private static final String ERROR = "error-general";

    public static String getMessage(final Context context, final String message) {
        switch (message) {
            case Messages.INVALID_USER:
                return context.getString(R.string.error_name);
            case Messages.INVALID_PASS:
                return context.getString(R.string.error_pass);
            case Messages.ERROR:
                return context.getString(R.string.error_general);
            default:
                return message;
        }
    }
}
