package me.echeung.moemoekyun.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.api.callback.LoginCallback
import me.echeung.moemoekyun.databinding.ActivityAuthLoginBinding
import me.echeung.moemoekyun.ui.base.BaseDataBindingActivity
import me.echeung.moemoekyun.util.system.clipboardManager
import me.echeung.moemoekyun.util.system.openUrl
import me.echeung.moemoekyun.util.system.toast

class AuthLoginActivity : BaseDataBindingActivity<ActivityAuthLoginBinding>() {

    private lateinit var loginCallback: LoginCallback
    private var mfaDialog: AlertDialog? = null

    init {
        layout = R.layout.activity_auth_login
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loginCallback = object : LoginCallback {
            override fun onSuccess(token: String) {
                runOnUiThread {
                    val returnIntent = Intent()
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                }
            }

            override fun onMfaRequired(token: String) {
                showMfaDialog()
            }

            override fun onFailure(message: String?) {
                runOnUiThread { applicationContext.toast(message) }
            }
        }

        binding.authBtn.setOnClickListener { _ -> login() }

        binding.authPassword.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login()
                return@OnEditorActionListener true
            }
            false
        })

        binding.forgotPassword.setOnClickListener { _ -> openUrl(FORGOT_PASSWORD_URL) }
    }

    public override fun onResume() {
        super.onResume()

        autoPasteMfaToken()
    }

    private fun login() {
        val userLogin = getText(binding.authLogin)
        val password = getText(binding.authPassword)

        setError(binding.authLogin, userLogin.isEmpty(), getString(R.string.required))
        setError(binding.authPassword, password.isEmpty(), getString(R.string.required))
        if (userLogin.isEmpty() || password.isEmpty()) {
            return
        }

        App.radioClient!!.api.authenticate(userLogin, password, loginCallback)
    }

    private fun showMfaDialog() {
        val layout = layoutInflater.inflate(R.layout.dialog_auth_mfa, findViewById(R.id.layout_root_mfa))
        val otpText = layout.findViewById<TextInputEditText>(R.id.mfa_otp)

        runOnUiThread {
            mfaDialog = AlertDialog.Builder(this, R.style.DialogTheme)
                    .setTitle(R.string.mfa_prompt)
                    .setView(layout)
                    .setPositiveButton(R.string.submit, fun(_, _) {
                        val otpToken = otpText.text.toString().trim { it <= ' ' }
                        if (otpToken.length != OTP_LENGTH) {
                            return
                        }

                        App.radioClient!!.api.authenticateMfa(otpToken, loginCallback)
                    })
                    .create()

            mfaDialog!!.show()
        }
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

    private fun getText(editText: TextInputEditText): String {
        return editText.text.toString().trim()
    }

    companion object {
        private const val OTP_LENGTH = 6
        private val OTP_REGEX = "^[0-9]*$".toRegex()
        private const val FORGOT_PASSWORD_URL = "https://listen.moe/login/forgot"
    }

}
