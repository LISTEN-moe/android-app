package me.echeung.moemoekyun.ui.activities;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import me.echeung.listenmoeapi.callbacks.LoginCallback;
import me.echeung.listenmoeapi.callbacks.RegisterCallback;
import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.databinding.ActivityAuthBinding;
import me.echeung.moemoekyun.viewmodels.AuthViewModel;

public class AuthActivity extends BaseActivity {

    private static final int OTP_LENGTH = 6;

    private ActivityAuthBinding binding;

    private LoginCallback loginCallback;

    private AlertDialog mfaDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_auth);

        final AuthViewModel viewModel = App.getAuthViewModel();

        binding.setVm(viewModel);

        setSupportActionBar(findViewById(R.id.appbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        binding.authBtn.setOnClickListener(v -> {
            if (viewModel.getShowRegister()) {
                register();
            } else {
                login();
            }
        });

        loginCallback = new LoginCallback() {
            @Override
            public void onSuccess(final String token) {
                runOnUiThread(() -> {
                    Intent returnIntent = new Intent();
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                });
            }

            @Override
            public void onMfaRequired(final String token) {
                showMfaDialog();
            }

            @Override
            public void onFailure(final String message) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();

        autoPasteMfaToken();
    }

    @Override
    protected void onDestroy() {
        if (binding != null) {
            binding.unbind();
        }

        super.onDestroy();
    }

    private void login() {
        final String userLogin = getText(binding.authLogin);
        final String password = getText(binding.authPassword);

        setError(binding.authLogin, userLogin.isEmpty(), getString(R.string.required));
        setError(binding.authPassword, password.isEmpty(), getString(R.string.required));
        if (userLogin.isEmpty() || password.isEmpty()) {
            return;
        }

        App.getApiClient().authenticate(userLogin, password, loginCallback);
    }

    private void showMfaDialog() {
        final View layout = getLayoutInflater().inflate(R.layout.dialog_auth_mfa, findViewById(R.id.layout_root_mfa));
        final TextInputEditText otpText = layout.findViewById(R.id.mfa_otp);

        runOnUiThread(() -> {
            mfaDialog = new AlertDialog.Builder(this, R.style.DialogTheme)
                    .setTitle(R.string.mfa_prompt)
                    .setView(layout)
                    .setPositiveButton(R.string.submit, (dialogInterface, i) -> {
                        final String otpToken = otpText.getText().toString().trim();
                        if (otpToken.length() != OTP_LENGTH) {
                            return;
                        }

                        App.getApiClient().authenticateMfa(otpToken, loginCallback);
                    })
                    .setNegativeButton(R.string.close, null)
                    .create();

            mfaDialog.show();
        });
    }

    private void autoPasteMfaToken() {
        if (mfaDialog == null || !mfaDialog.isShowing()) {
            return;
        }

        final ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboard == null) {
            return;
        }

        final ClipData clipData = clipboard.getPrimaryClip();
        if (clipData == null || clipData.getItemCount() == 0) {
            return;
        }

        final ClipData.Item clipDataItem = clipData.getItemAt(0);
        final String clipboardText = clipDataItem.getText().toString();

        if (clipboardText.length() == OTP_LENGTH && clipboardText.matches("^[0-9]*$")) {
            final TextInputEditText otpText = mfaDialog.findViewById(R.id.mfa_otp);
            if (otpText != null) {
                otpText.setText(clipboardText);
            }
        }
    }

    private void register() {
        final String username = getText(binding.authUsername);
        final String email = getText(binding.authEmail);
        final String password = getText(binding.authPassword);
        final String passwordConfirm = getText(binding.authPasswordConfirm);

        setError(binding.authUsername, username.isEmpty(), getString(R.string.required));
        setError(binding.authEmail, email.isEmpty(), getString(R.string.required));
        setError(binding.authPassword, password.isEmpty(), getString(R.string.required));
        setError(binding.authPasswordConfirm, passwordConfirm.isEmpty(), getString(R.string.required));
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
            return;
        }

        setError(binding.authPassword, !password.equals(passwordConfirm), getString(R.string.password_mismatch));
        setError(binding.authPasswordConfirm, !password.equals(passwordConfirm), getString(R.string.password_mismatch));
        if (!password.equals(passwordConfirm)) {
            return;
        }

        App.getApiClient().register(email, username, password, new RegisterCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onFailure(String message) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void setError(TextInputEditText editText, boolean isError, String errorMessage) {
        editText.setError(isError ? errorMessage : null);
    }

    private String getText(TextInputEditText editText) {
        return editText.getText().toString().trim();
    }

}
