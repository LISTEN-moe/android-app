package me.echeung.moemoekyun.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import me.echeung.listenmoeapi.callbacks.AuthCallback;
import me.echeung.listenmoeapi.responses.Messages;
import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.adapters.ViewPagerAdapter;
import me.echeung.moemoekyun.databinding.ActivityMainBinding;
import me.echeung.moemoekyun.service.RadioService;
import me.echeung.moemoekyun.ui.dialogs.LoginDialog;
import me.echeung.moemoekyun.ui.dialogs.SleepTimerDialog;
import me.echeung.moemoekyun.utils.NetworkUtil;
import me.echeung.moemoekyun.utils.UrlUtil;

public class MainActivity extends BaseActivity {

    public static final String TRIGGER_LOGIN_AND_FAVORITE = "fav_after_login";
    public static final String AUTH_EVENT = "auth_event";

    private static final String URL_REGISTER = "https://listen.moe/#/register";

    private ActivityMainBinding binding;

    private ViewPager viewPager;

    private BroadcastReceiver intentReceiver;
    private boolean receiverRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.setVm(App.getRadioViewModel());

        // Check network connectivity
        binding.btnRetry.setOnClickListener(v -> retry());
        if (!NetworkUtil.isNetworkAvailable(this)) {
            return;
        }

        // Init app/tab bar
        initAppbar();

        // Sets audio type to media (volume button control)
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Invalidate token if needed
        if (!App.getAuthUtil().checkAuthTokenValidity()) {
            App.getUserViewModel().reset();
        }

        // Handle intent actions
        initBroadcastReceiver();
        handleIntentAction(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntentAction(intent);
    }

    @Override
    protected void onDestroy() {
        if (receiverRegistered) {
            unregisterReceiver(intentReceiver);
            receiverRegistered = false;
        }

        // Kill service/notification if killing activity and not playing
        final RadioService service = App.getService();
        if (service != null && !service.isPlaying()) {
            final Intent stopIntent = new Intent(RadioService.STOP);
            sendBroadcast(stopIntent);
        }

        if (viewPager != null) {
            viewPager.setAdapter(null);
        }

        if (binding != null) {
            binding.unbind();
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (viewPager != null && viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem(0, true);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * For retry button in no internet view.
     */
    private void retry() {
        if (NetworkUtil.isNetworkAvailable(this)) {
            recreate();

            final Intent updateIntent = new Intent(RadioService.UPDATE);
            sendBroadcast(updateIntent);
        }
    }

    private void initBroadcastReceiver() {
        intentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleIntentAction(intent);
            }
        };

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.TRIGGER_LOGIN_AND_FAVORITE);

        registerReceiver(intentReceiver, intentFilter);
        receiverRegistered = true;
    }

    private void handleIntentAction(Intent intent) {
        final String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case MainActivity.TRIGGER_LOGIN_AND_FAVORITE:
                    showLoginDialog(this::favoriteSong);
                    break;
            }
        }
    }

    /**
     * Initializes everything for the tabs: the adapter, icons, and title handler
     */
    private void initAppbar() {
        // Set up app bar
        setSupportActionBar(binding.appbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Set up ViewPager and adapter
        viewPager = binding.pager;
        final ViewPagerAdapter mViewPagerAdapter = new ViewPagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(mViewPagerAdapter);

        // Set up tabs
        final TabLayout tabLayout = binding.tabs;
        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        final int tabCount = tabLayout.getTabCount();
        for (int i = 0; i < tabCount; i++) {
            tabLayout.getTabAt(i).setIcon(mViewPagerAdapter.getIcon(i));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Toggle visibility of login/logout items based on authentication status
        final boolean authenticated = App.getAuthUtil().isAuthenticated();
        menu.findItem(R.id.action_login).setVisible(!authenticated);
        menu.findItem(R.id.action_register).setVisible(!authenticated);
        menu.findItem(R.id.action_logout).setVisible(authenticated);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                if (App.getAuthUtil().isAuthenticated()) {
                    startActivity(new Intent(this, SearchActivity.class));
                } else {
                    showLoginDialog(() -> onOptionsItemSelected(item));
                }
                return true;

            case R.id.action_login:
                showLoginDialog();
                return true;

            case R.id.action_register:
                UrlUtil.openUrl(this, URL_REGISTER);
                return true;

            case R.id.action_logout:
                showLogoutDialog();
                return true;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;

            case R.id.action_sleep_timer:
                new SleepTimerDialog(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showLoginDialog() {
        showLoginDialog(null);
    }

    public void showLoginDialog(@Nullable final LoginDialog.OnLoginListener listener) {
        new LoginDialog(this, (user, pass, dialog) -> App.getApiClient().authenticate(user, pass, new AuthCallback() {
            @Override
            public void onSuccess(final String result) {
                runOnUiThread(() -> {
                    dialog.dismiss();
                    invalidateOptionsMenu();

                    App.getAuthUtil().setAuthToken(result);
                    broadcastAuthEvent();

                    if (listener != null) {
                        listener.onLogin();
                    }
                });
            }

            @Override
            public void onFailure(final String message) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), getAuthMessage(message), Toast.LENGTH_LONG).show());
            }
        }));
    }

    private void broadcastAuthEvent() {
        final Intent authEventIntent = new Intent(MainActivity.AUTH_EVENT);
        sendBroadcast(authEventIntent);
    }

    private void favoriteSong() {
        final Intent favIntent = new Intent(RadioService.TOGGLE_FAVORITE);
        sendBroadcast(favIntent);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle(R.string.logout)
                .setMessage(getString(R.string.logout_confirmation))
                .setPositiveButton(R.string.logout, (dialogInterface, i) -> logout())
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }

    private void logout() {
        if (!App.getAuthUtil().isAuthenticated()) {
            return;
        }

        App.getAuthUtil().clearAuthToken();
        App.getUserViewModel().reset();

        Toast.makeText(getApplicationContext(), getString(R.string.logged_out), Toast.LENGTH_LONG).show();
        invalidateOptionsMenu();

        broadcastAuthEvent();
    }

    private String getAuthMessage(final String message) {
        switch (message) {
            case Messages.INVALID_USER:
                return getString(R.string.auth_error_name);
            case Messages.INVALID_PASS:
                return getString(R.string.auth_error_pass);
            case Messages.ERROR:
                return getString(R.string.auth_error_general);
            default:
                return message;
        }
    }
}
