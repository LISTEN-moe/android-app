package me.echeung.moemoekyun.ui.activity.auth

import android.app.Activity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.client.api.APIClient
import me.echeung.moemoekyun.databinding.ActivityAuthLoginBinding
import me.echeung.moemoekyun.ui.base.BaseActivity
import me.echeung.moemoekyun.util.ext.clipboardManager
import me.echeung.moemoekyun.util.ext.finish
import me.echeung.moemoekyun.util.ext.getTrimmedText
import me.echeung.moemoekyun.util.ext.launchIO
import me.echeung.moemoekyun.util.ext.launchUI
import me.echeung.moemoekyun.util.ext.toast
import org.koin.android.ext.android.inject

class AuthLoginActivity : BaseActivity() {

    private val radioClient: RadioClient by inject()

    private var mfaDialog: AlertDialog? = null

    private lateinit var binding: ActivityAuthLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initAppbar()

        binding.authBtn.setOnClickListener { login() }

        binding.authPassword.setOnEditorActionListener(
            TextView.OnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    login()
                    return@OnEditorActionListener true
                }
                false
            }
        )

        // Set fields from registration
        if (intent?.getStringExtra(AuthActivityUtil.LOGIN_NAME) != null) {
            binding.authLogin.setText(intent.getStringExtra(AuthActivityUtil.LOGIN_NAME))
        }
        if (intent?.getStringExtra(AuthActivityUtil.LOGIN_PASS) != null) {
            binding.authPassword.setText(intent.getStringExtra(AuthActivityUtil.LOGIN_PASS))
        }
    }

    public override fun onResume() {
        super.onResume()

        autoPasteMfaToken()
    }

    private fun login() {
        val userLogin = binding.authLogin.getTrimmedText()
        val password = binding.authPassword.getTrimmedText()

        setError(binding.authLogin, userLogin.isEmpty(), getString(R.string.required))
        setError(binding.authPassword, password.isEmpty(), getString(R.string.required))
        if (userLogin.isEmpty() || password.isEmpty()) {
            return
        }

        launchIO {
            try {
                val result = radioClient.api.authenticate(userLogin, password)
                when (result.first) {
                    APIClient.LoginState.REQUIRE_OTP -> {
                        launchUI { showMfaDialog() }
                    }
                    APIClient.LoginState.COMPLETE -> {
                        launchUI { finish(Activity.RESULT_OK) }
                    }
                }
            } catch (e: Exception) {
                launchUI { toast(e.message) }
            }
        }
    }

    private fun showMfaDialog() {
        val layout =
            layoutInflater.inflate(R.layout.dialog_auth_mfa, findViewById(R.id.layout_root_mfa))
        val otpText = layout.findViewById<TextInputEditText>(R.id.mfa_otp)

        mfaDialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.mfa_prompt)
            .setView(layout)
            .setPositiveButton(
                R.string.submit
            ) { _, _ ->
                val otpToken = otpText.text.toString().trim { it <= ' ' }
                if (otpToken.length == OTP_LENGTH) {
                    launchIO {
                        try {
                            radioClient.api.authenticateMfa(otpToken)
                            launchUI { finish(Activity.RESULT_OK) }
                        } catch (e: Exception) {
                            launchUI { toast(e.message) }
                        }
                    }
                }
            }
            .create()

        mfaDialog!!.show()
    }

    private fun autoPasteMfaToken() {
        if (mfaDialog == null || !mfaDialog!!.isShowing) {
            return
        }

        val clipData = clipboardManager.primaryClip
        if (clipData == null || clipData.itemCount == 0) {
            return
        }

        val clipDataItem = clipData.getItemAt(0)
        val clipboardText = clipDataItem.text.toString()

        if (clipboardText.length == OTP_LENGTH && clipboardText.matches(OTP_REGEX)) {
            val otpText = mfaDialog!!.findViewById<TextInputEditText>(R.id.mfa_otp)
            otpText?.setText(clipboardText)
        }
    }

    private fun setError(editText: TextInputEditText, isError: Boolean, errorMessage: String) {
        editText.error = if (isError) errorMessage else null
    }
}

private const val OTP_LENGTH = 6
private val OTP_REGEX = "^[0-9]*$".toRegex()
