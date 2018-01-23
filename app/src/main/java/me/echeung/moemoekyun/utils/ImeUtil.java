package me.echeung.moemoekyun.utils;

import android.app.Activity;
import android.view.inputmethod.InputMethodManager;

public final class ImeUtil {

    public static void hideSoftKeyboard(Activity activity) {
        if (activity == null) {
            return;
        }

        final InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && inputMethodManager.isActive() && activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

}
