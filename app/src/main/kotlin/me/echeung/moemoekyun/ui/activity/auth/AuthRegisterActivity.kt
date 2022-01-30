package me.echeung.moemoekyun.ui.activity.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.databinding.ActivityAuthRegisterBinding
import me.echeung.moemoekyun.ui.base.BaseActivity
import me.echeung.moemoekyun.util.ext.launchIO
import me.echeung.moemoekyun.util.ext.launchUI
import me.echeung.moemoekyun.util.ext.toast
import org.koin.android.ext.android.inject

class AuthRegisterActivity : BaseActivity() {

    private val radioClient: RadioClient by inject()

    private lateinit var binding: ActivityAuthRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initAppbar()

        binding.authBtn.setOnClickListener { register() }

        binding.authPasswordConfirm.setOnEditorActionListener(
            TextView.OnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    register()
                    return@OnEditorActionListener true
                }
                false
            }
        )
    }

    private fun register() {
        val username = getText(binding.authUsername)
        val email = getText(binding.authEmail)
        val password = getText(binding.authPassword)
        val passwordConfirm = getText(binding.authPasswordConfirm)

        setError(binding.authUsername, username.isEmpty(), getString(R.string.required))
        setError(binding.authEmail, email.isEmpty(), getString(R.string.required))
        setError(binding.authPassword, password.isEmpty(), getString(R.string.required))
        setError(binding.authPasswordConfirm, passwordConfirm.isEmpty(), getString(R.string.required))
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
            return
        }

        setError(binding.authPassword, password != passwordConfirm, getString(R.string.password_mismatch))
        setError(binding.authPasswordConfirm, password != passwordConfirm, getString(R.string.password_mismatch))
        if (password != passwordConfirm) {
            return
        }

        launchIO {
            try {
                radioClient.api.register(email, username, password)

                launchUI {
                    val returnIntent = Intent()
                    returnIntent.putExtra(AuthActivityUtil.LOGIN_NAME, username)
                    returnIntent.putExtra(AuthActivityUtil.LOGIN_PASS, password)
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                }
            } catch (e: Exception) {
                launchUI { toast(e.message) }
            }
        }
    }

    private fun setError(editText: TextInputEditText, isError: Boolean, errorMessage: String) {
        editText.error = if (isError) errorMessage else null
    }

    private fun getText(editText: TextInputEditText): String {
        return editText.text.toString().trim()
    }
}
