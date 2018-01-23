package me.echeung.moemoekyun.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.widget.Toast;

import me.echeung.listenmoeapi.callbacks.AuthCallback;
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

        if (user.isEmpty() || pass.isEmpty()) {
            return;
        }

        AuthCallback callback = new AuthCallback() {
            @Override
            public void onSuccess(final String token) {
                App.getAuthUtil().setAuthToken(token);

                runOnUiThread(() -> {
                    Intent returnIntent = new Intent();
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                });
            }

            @Override
            public void onMfaRequired(final String token) {
                App.getAuthUtil().setAuthToken(token);
                viewModel.setShowMfa(true);
            }

            @Override
            public void onFailure(final String message) {
                viewModel.setShowMfa(false);

                runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show());
            }
        };

        // Login with OTP token
        if (viewModel.getShowMfa()) {
            final String otpToken = binding.authOtp.getText().toString().trim();

            if (otpToken.length() != 6) {
                return;
            }

            App.getApiClient().authenticateMfa(otpToken, callback);
            return;
        }


        // Login
        App.getApiClient().authenticate(user, pass, callback);
    }

}
