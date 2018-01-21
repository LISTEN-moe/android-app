package me.echeung.moemoekyun.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.widget.Toast;

import me.echeung.listenmoeapi.callbacks.AuthCallback;
import me.echeung.listenmoeapi.responses.Messages;
import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.databinding.ActivityAuthBinding;
import me.echeung.moemoekyun.viewmodels.AuthViewModel;

public class AuthActivity extends BaseActivity {

    private ActivityAuthBinding binding;

    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_auth);

        viewModel = App.getAuthViewModel();
        viewModel.reset();

        binding.setVm(viewModel);

        setSupportActionBar(findViewById(R.id.appbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.authBtn.setOnClickListener(v -> login());
    }

    @Override
    protected void onDestroy() {
        if (binding != null) {
            binding.unbind();
        }

        super.onDestroy();
    }

    private void login() {
        final String user = binding.authUsername.getText().toString().trim();
        final String pass = binding.authPassword.getText().toString().trim();

        if (user.length() == 0 || pass.length() == 0) {
            return;
        }

        App.getApiClient().authenticate(user, pass, new AuthCallback() {
            @Override
            public void onSuccess(final String token) {
                runOnUiThread(() -> {
                    App.getAuthUtil().setAuthToken(token);

                    Intent returnIntent = new Intent();
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                });
            }

            @Override
            public void onMfaRequired(final String token) {
                // TODO: MFA
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
