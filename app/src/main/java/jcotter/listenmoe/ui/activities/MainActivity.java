package jcotter.listenmoe.ui.activities;

import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import jcotter.listenmoe.R;
import jcotter.listenmoe.adapters.ViewPagerAdapter;
import jcotter.listenmoe.constants.ResponseMessages;
import jcotter.listenmoe.interfaces.AuthCallback;
import jcotter.listenmoe.service.StreamService;
import jcotter.listenmoe.util.APIUtil;
import jcotter.listenmoe.util.AuthUtil;
import jcotter.listenmoe.util.SDKUtil;

public class MainActivity extends AppCompatActivity {
    public static final String TRIGGER_LOGIN = "trigger_login";

    private AlertDialog mAboutDialog;

    public static BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init app/tab bar
        initToolbar();

        // Sets audio type to media (volume button control)
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Invalidate token if needed
        AuthUtil.checkAuthTokenValidity(this);

        if (getIntent() != null) {
            final String action = getIntent().getAction();

            if (action.equals(MainActivity.TRIGGER_LOGIN)) {
                showLoginDialog();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // TODO: use flag to track if registered
        try {
            unregisterReceiver(MainActivity.broadcastReceiver);
        } catch (IllegalArgumentException ignored) {
        }

        final Intent intent = new Intent(getBaseContext(), StreamService.class);
        intent.putExtra(StreamService.KILLABLE, true);
        startService(intent);
    }


    // Tabs

    /**
     * Initializes everything for the tabs: the adapter, icons, and title handler
     */
    private void initToolbar() {
        // Set up app bar
        setSupportActionBar((Toolbar) findViewById(R.id.appbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Set up ViewPager and adapter
        final ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        final ViewPagerAdapter mViewPagerAdapter =
                new ViewPagerAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(mViewPagerAdapter);

        // Set up tabs
        final TabLayout mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
    }


    // Overflow menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Toggle visibility of login/logout items based on authentication status
        final boolean authenticated = AuthUtil.isAuthenticated(this);
        menu.findItem(R.id.action_login).setVisible(!authenticated);
        menu.findItem(R.id.action_logout).setVisible(authenticated);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                startActivity(new Intent(this, SearchActivity.class));
                return true;

            case R.id.action_login:
                showLoginDialog();
                return true;

            case R.id.action_logout:
                showLogoutDialog();
                return true;

            case R.id.action_about:
                showAboutDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showLoginDialog() {
        final View layout = getLayoutInflater().inflate(R.layout.dialog_login, (ViewGroup) findViewById(R.id.layout_root));
        final EditText mLoginUser = (EditText) layout.findViewById(R.id.login_username);
        final EditText mLoginPass = (EditText) layout.findViewById(R.id.login_password);

        final AlertDialog loginDialog = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle)
                .setTitle(R.string.login)
                .setView(layout)
                .setPositiveButton(R.string.login, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        // Override the positive button listener so it won't automatically be dismissed even with
        // an error
        loginDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                final Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String user = mLoginUser.getText().toString().trim();
                        final String pass = mLoginPass.getText().toString().trim();

                        if (user.length() == 0 || pass.length() == 0) {
                            return;
                        }

                        login(user, pass, dialog);
                    }
                });
            }
        });

        loginDialog.show();
    }

    /**
     * Logs the user in with the provided credentials.
     *
     * @param user   Username to pass in the request body.
     * @param pass   Password to pass in the request body.
     * @param dialog Reference to the login dialog so it can be dismissed upon success.
     */
    private void login(final String user, final String pass, final DialogInterface dialog) {
        APIUtil.authenticate(this, user, pass, new AuthCallback() {
            @Override
            public void onFailure(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String errorMsg = "";

                        switch (result) {
                            case ResponseMessages.INVALID_USER:
                                errorMsg = getString(R.string.error_name);
                                break;
                            case ResponseMessages.INVALID_PASS:
                                errorMsg = getString(R.string.error_pass);
                                break;
                            case ResponseMessages.ERROR:
                                errorMsg = getString(R.string.error_general);
                                break;
                        }

                        Toast.makeText(getBaseContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onSuccess(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(), getString(R.string.success), Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        invalidateOptionsMenu();

                        // TODO: update notification?
                    }
                });
            }
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle)
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirmation)
                .setPositiveButton(R.string.logout, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        logout();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }

    /**
     * Logs the user out.
     */
    private void logout() {
        if (!AuthUtil.isAuthenticated(this)) {
            return;
        }

        AuthUtil.clearAuthToken(this);
        Toast.makeText(getBaseContext(), getString(R.string.logged_out), Toast.LENGTH_LONG).show();
        invalidateOptionsMenu();
    }

    private void showAboutDialog() {
        if (mAboutDialog == null) {
            String version;
            try {
                version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                version = "";
            }

            mAboutDialog = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle)
                    .setTitle(R.string.about)
                    .setMessage(SDKUtil.fromHtml(getString(R.string.about_content, getString(R.string.app_name), version)))
                    .setPositiveButton(R.string.close, null)
                    .create();
        }

        mAboutDialog.show();

        final TextView textContent = (TextView) mAboutDialog.findViewById(android.R.id.message);
        textContent.setMovementMethod(LinkMovementMethod.getInstance());
    }
}