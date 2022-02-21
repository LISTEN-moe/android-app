package me.echeung.moemoekyun.ui.activity.auth

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.api.auth.AuthUtil
import me.echeung.moemoekyun.service.RadioService
import me.echeung.moemoekyun.util.ext.toast
import me.echeung.moemoekyun.viewmodel.RadioViewModel
import me.echeung.moemoekyun.viewmodel.UserViewModel
import org.koin.android.ext.android.get
import org.koin.core.component.KoinComponent

object AuthActivityUtil : KoinComponent {

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
        sendBroadcast(Intent(AUTH_EVENT).apply {
            setPackage(applicationContext.packageName)
        })

        val radioViewModel: RadioViewModel = get()
        val authUtil: AuthUtil = get()

        radioViewModel.isAuthed = authUtil.isAuthenticated
    }

    fun FragmentActivity.showLogoutDialog() {
        MaterialAlertDialogBuilder(this)
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
            LOGIN_FAVORITE_REQUEST -> sendBroadcast(Intent(RadioService.TOGGLE_FAVORITE).apply {
                setPackage(packageName)
            })
        }
    }

    private fun FragmentActivity.logout() {
        val authUtil: AuthUtil = get()

        if (!authUtil.isAuthenticated) {
            return
        }

        authUtil.clearAuthToken()

        val userViewModel: UserViewModel = get()
        userViewModel.reset()

        toast(getString(R.string.logged_out), Toast.LENGTH_LONG)
        invalidateOptionsMenu()

        broadcastAuthEvent()
    }
}
