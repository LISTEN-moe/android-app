package me.echeung.moemoekyun.ui.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;

import me.echeung.moemoekyun.R;

public class LoginDialog {

    private Activity activity;
    private Callback callback;

    public LoginDialog(@NonNull Activity activity, @NonNull Callback callback) {
        this.activity = activity;
        this.callback = callback;

        initDialog();
    }

    private void initDialog() {
        final View layout = activity.getLayoutInflater().inflate(R.layout.dialog_login, activity.findViewById(R.id.layout_root_login));
        final TextInputEditText loginUser = layout.findViewById(R.id.login_username);
        final TextInputEditText loginPass = layout.findViewById(R.id.login_password);

        final AlertDialog loginDialog = new AlertDialog.Builder(activity, R.style.DialogTheme)
                .setTitle(R.string.login)
                .setView(layout)
                .setPositiveButton(R.string.login, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        // Override the positive button listener so it won't automatically be dismissed even with
        // an error
        loginDialog.setOnShowListener(dialog -> {
            final Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                final String user = loginUser.getText().toString().trim();
                final String pass = loginPass.getText().toString().trim();

                if (user.length() == 0 || pass.length() == 0) {
                    return;
                }

                callback.login(user, pass, dialog);
            });
        });

        // Login when keyboard "done" action is pressed on password field
        loginPass.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                return true;
            }
            return false;
        });

        loginDialog.show();
    }

    public interface Callback {
        void login(final String user, final String pass, final DialogInterface dialog);
    }

    public interface OnLoginListener {
        void onLogin();
    }
}
