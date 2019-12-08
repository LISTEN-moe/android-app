package me.echeung.moemoekyun.util

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.service.RadioService
import me.echeung.moemoekyun.ui.activity.AuthLoginActivity
import me.echeung.moemoekyun.ui.activity.AuthRegisterActivity
import me.echeung.moemoekyun.util.ext.toast
import me.echeung.moemoekyun.viewmodel.UserViewModel
import org.koin.android.ext.android.get

object AuthActivityUtil {
    // Intent codes
    const val LOGIN_REQUEST = 0
    const val LOGIN_FAVORITE_REQUEST = 1
    const val REGISTER_REQUEST = 2

    // Intent extra keys
    const val LOGIN_NAME = "login_name"
    const val LOGIN_PASS = "login_pass"

    const val AUTH_EVENT = "auth_event"

    fun FragmentActivity.showLoginActivity(requestCode: Int = LOGIN_REQUEST) {
        startActivityForResult(Intent(this, AuthLoginActivity::class.java), requestCode)
    }

    fun FragmentActivity.showLoginActivity(intent: Intent) {
        intent.setClass(this, AuthLoginActivity::class.java)
        startActivityForResult(intent, LOGIN_REQUEST)
    }

    fun FragmentActivity.showRegisterActivity() {
        startActivityForResult(Intent(this, AuthRegisterActivity::class.java), REGISTER_REQUEST)
    }

    fun FragmentActivity.broadcastAuthEvent() {
        sendBroadcast(Intent(AUTH_EVENT))

        App.radioViewModel?.isAuthed = App.authTokenUtil.isAuthenticated
    }

    fun FragmentActivity.showLogoutDialog() {
        AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle(R.string.logout)
                .setMessage(getString(R.string.logout_confirmation))
                .setPositiveButton(R.string.logout) { _, _ -> logout() }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show()
    }

    fun FragmentActivity.handleAuthActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        invalidateOptionsMenu()
        broadcastAuthEvent()

        when (requestCode) {
            // Redirect to login after registering
            REGISTER_REQUEST -> showLoginActivity(data!!)

            // Favorite song after logging in
            LOGIN_FAVORITE_REQUEST -> sendBroadcast(Intent(RadioService.TOGGLE_FAVORITE))
        }
    }

    private fun FragmentActivity.logout() {
        if (!App.authTokenUtil.isAuthenticated) {
            return
        }

        App.authTokenUtil.clearAuthToken()

        val userViewModel: UserViewModel = get()
        userViewModel.reset()

        applicationContext.toast(getString(R.string.logged_out), Toast.LENGTH_LONG)
        invalidateOptionsMenu()

        broadcastAuthEvent()
    }
}
