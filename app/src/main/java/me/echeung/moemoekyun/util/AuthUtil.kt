package me.echeung.moemoekyun.util

import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.activity.AuthLoginActivity
import me.echeung.moemoekyun.ui.activity.AuthRegisterActivity
import me.echeung.moemoekyun.util.system.startActivity
import me.echeung.moemoekyun.util.system.toast

object AuthUtil {
    const val LOGIN_REQUEST = 0
    const val LOGIN_FAVORITE_REQUEST = 1

    const val AUTH_EVENT = "auth_event"

    fun FragmentActivity.showLoginActivity(requestCode: Int = LOGIN_REQUEST) {
        startActivityForResult(Intent(this, AuthLoginActivity::class.java), requestCode)
    }

    fun FragmentActivity.showRegisterActivity() {
        startActivity<AuthRegisterActivity>(this)
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

    private fun FragmentActivity.logout() {
        if (!App.authTokenUtil.isAuthenticated) {
            return
        }

        App.authTokenUtil.clearAuthToken()
        App.userViewModel!!.reset()

        applicationContext.toast(getString(R.string.logged_out), Toast.LENGTH_LONG)
        invalidateOptionsMenu()

        broadcastAuthEvent()
    }
}
