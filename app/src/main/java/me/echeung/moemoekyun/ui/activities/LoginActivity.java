package me.echeung.moemoekyun.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.widget.Toast;

import me.echeung.listenmoeapi.callbacks.AuthCallback;
import me.echeung.listenmoeapi.responses.Messages;
import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;

public class LoginActivity extends BaseActivity {

    private TextInputEditText loginUser;
    private TextInputEditText loginPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setSupportActionBar(findViewById(R.id.appbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loginUser = findViewById(R.id.login_username);
        loginPass = findViewById(R.id.login_password);

        findViewById(R.id.login_btn).setOnClickListener(v -> login());
    }

    private void login() {
        final String user = loginUser.getText().toString().trim();
        final String pass = loginPass.getText().toString().trim();

        if (user.length() == 0 || pass.length() == 0) {
            return;
        }

        App.getApiClient().authenticate(user, pass, new AuthCallback() {
            @Override
            public void onSuccess(final String result) {
                runOnUiThread(() -> {
                    App.getAuthUtil().setAuthToken(result);

                    Intent returnIntent = new Intent();
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                });
            }

            @Override
            public void onFailure(final String message) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), getAuthMessage(message), Toast.LENGTH_LONG).show());
            }
        });
    }

    private String getAuthMessage(final String message) {
        switch (message) {
            case Messages.INVALID_USER:
                return getString(R.string.auth_error_name);
            case Messages.INVALID_PASS:
                return getString(R.string.auth_error_pass);
            case Messages.ERROR:
                return getString(R.string.auth_error_general);
            default:
                return message;
        }
    }
}
