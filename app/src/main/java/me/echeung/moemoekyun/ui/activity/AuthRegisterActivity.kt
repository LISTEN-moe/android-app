package me.echeung.moemoekyun.ui.activity

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.google.android.material.textfield.TextInputEditText
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.api.callback.RegisterCallback
import me.echeung.moemoekyun.databinding.ActivityAuthRegisterBinding
import me.echeung.moemoekyun.ui.base.BaseActivity
import me.echeung.moemoekyun.util.system.toast

class AuthRegisterActivity : BaseActivity() {

    private lateinit var binding: ActivityAuthRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_auth_register)

        setSupportActionBar(findViewById(R.id.appbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        binding.authBtn.setOnClickListener { v -> submit() }

        val onSubmit = TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
                return@OnEditorActionListener true
            }
            false
        }

        binding.authPasswordConfirm.setOnEditorActionListener(onSubmit)
    }

    override fun onDestroy() {
        binding?.unbind()

        super.onDestroy()
    }

    private fun submit() {
        register()
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

        App.radioClient!!.api.register(email, username, password, object : RegisterCallback {
            override fun onSuccess(message: String) {
                runOnUiThread { applicationContext.toast(message) }
            }

            override fun onFailure(message: String) {
                runOnUiThread { applicationContext.toast(message) }
            }
        })
    }

    private fun setError(editText: TextInputEditText, isError: Boolean, errorMessage: String) {
        editText.error = if (isError) errorMessage else null
    }

    private fun getText(editText: TextInputEditText): String {
        return editText.text.toString().trim()
    }

}
