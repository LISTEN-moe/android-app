package me.echeung.moemoekyun.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import me.echeung.listenmoeapi.callbacks.AuthCallback;
import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.databinding.ActivityAuthBinding;
import me.echeung.moemoekyun.viewmodels.AuthViewModel;

public class AuthActivity extends BaseActivity {

    private static final int OTP_LENGTH = 6;

    private ActivityAuthBinding binding;

    private AuthViewModel viewModel;

    private AuthCallback callback;

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

        callback = new AuthCallback() {
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
                showMfaDialog();
            }

            @Override
            public void onFailure(final String message) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show());
            }
        };
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

        App.getApiClient().authenticate(user, pass, callback);
    }

    private void showMfaDialog() {
        final View layout = getLayoutInflater().inflate(R.layout.dialog_auth_mfa, findViewById(R.id.layout_root_mfa));
        final TextInputEditText otpText = layout.findViewById(R.id.mfa_otp);

        runOnUiThread(() -> new AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle(R.string.mfa_prompt)
                .setView(layout)
                .setPositiveButton(R.string.submit, (dialogInterface, i) -> {
                    final String otpToken = otpText.getText().toString().trim();
                    if (otpToken.length() != OTP_LENGTH) {
                        return;
                    }

                    App.getApiClient().authenticateMfa(otpToken, callback);
                })
                .setNegativeButton(R.string.close, null)
                .create()
                .show());
    }

}
