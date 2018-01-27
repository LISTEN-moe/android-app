package me.echeung.moemoekyun.ui.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.BR;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.adapters.ViewPagerAdapter;
import me.echeung.moemoekyun.databinding.ActivityMainBinding;
import me.echeung.moemoekyun.service.RadioService;
import me.echeung.moemoekyun.ui.dialogs.SleepTimerDialog;
import me.echeung.moemoekyun.utils.NetworkUtil;
import me.echeung.moemoekyun.utils.SongActionsUtil;
import me.echeung.moemoekyun.utils.UrlUtil;
import me.echeung.moemoekyun.viewmodels.RadioViewModel;

public class MainActivity extends BaseActivity {

    public static final String AUTH_EVENT = "auth_event";

    public static final int LOGIN_REQUEST = 0;
    public static final int LOGIN_FAVORITE_REQUEST = 1;

    private static final String URL_REGISTER = "https://listen.moe/#/register";

    private ActivityMainBinding binding;

    private RadioViewModel viewModel;

    private ViewPager viewPager;

    private Observable.OnPropertyChangedCallback playPauseCallback;
    private FloatingActionButton vPlayPauseBtn;
    private AnimatedVectorDrawable playToPause;
    private AnimatedVectorDrawable pauseToPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        viewModel = App.getRadioViewModel();
        binding.setVm(viewModel);

        binding.btnRetry.setOnClickListener(v -> retry());
        binding.btnLogin.setOnClickListener(v -> showAuthActivity());

        // Check network connectivity
        if (!NetworkUtil.isNetworkAvailable(this)) {
            return;
        }

        // Sets audio type to media (volume button control)
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Init app/tab bar
        initAppbar();

        // Init now playing sheet
        initNowPlaying();

        // Invalidate token if needed
        boolean isAuthed = App.getAuthUtil().checkAuthTokenValidity();
        viewModel.setIsAuthed(isAuthed);
        if (!isAuthed) {
            App.getUserViewModel().reset();
        }
    }

    @Override
    protected void onDestroy() {
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

        if (playPauseCallback != null) {
            viewModel.removeOnPropertyChangedCallback(playPauseCallback);
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // Prevent app from exiting when pressing back on the user tab
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

    private void initNowPlaying() {
        // Clickable links
        final TextView vRequestBy = binding.nowPlaying.radioControls.requestedBy;
        vRequestBy.setMovementMethod(LinkMovementMethod.getInstance());

        initPlayPause();

        final ImageButton vHistoryBtn = binding.nowPlaying.radioControls.historyBtn;
        vHistoryBtn.setOnClickListener(v -> showHistory());

        final ImageButton vFavoriteBtn = binding.nowPlaying.radioControls.favoriteBtn;
        vFavoriteBtn.setOnClickListener(v -> favorite());

        binding.nowPlaying.radioSongs.songList1.setOnLongClickListener(v -> {
            SongActionsUtil.copyToClipboard(this, viewModel.getCurrentSong());
            return true;
        });
        binding.nowPlaying.radioSongs.songList2.setOnLongClickListener(v -> {
            SongActionsUtil.copyToClipboard(this, viewModel.getLastSong());
            return true;
        });
        binding.nowPlaying.radioSongs.songList3.setOnLongClickListener(v -> {
            SongActionsUtil.copyToClipboard(this, viewModel.getSecondLastSong());
            return true;
        });
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
            case R.id.action_login:
                showAuthActivity();
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


    // Auth stuff
    // =============================================================================================

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        invalidateOptionsMenu();
        broadcastAuthEvent();

        switch (requestCode) {
            case LOGIN_FAVORITE_REQUEST:
                final Intent favIntent = new Intent(RadioService.TOGGLE_FAVORITE);
                sendBroadcast(favIntent);
                break;
        }
    }

    public void showAuthActivity() {
        startActivityForResult(new Intent(this, AuthActivity.class), LOGIN_REQUEST);
    }

    public void showAuthActivity(int requestCode) {
        startActivityForResult(new Intent(this, AuthActivity.class), requestCode);
    }

    private void broadcastAuthEvent() {
        final Intent authEventIntent = new Intent(MainActivity.AUTH_EVENT);
        sendBroadcast(authEventIntent);

        viewModel.setIsAuthed(App.getAuthUtil().isAuthenticated());
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


    // Now playing stuff
    // =============================================================================================

    private void initPlayPause() {
        vPlayPauseBtn = binding.nowPlaying.radioControls.playPauseBtn;
        vPlayPauseBtn.setOnClickListener(v -> togglePlayPause());

        playToPause = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_play_to_pause);
        pauseToPlay = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_pause_to_play);

        setPlayPauseDrawable();

        playPauseCallback = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (propertyId == BR.isPlaying) {
                    setPlayPauseDrawable();
                }
            }
        };

        viewModel.addOnPropertyChangedCallback(playPauseCallback);
    }

    private void setPlayPauseDrawable() {
        final AnimatedVectorDrawable drawable = viewModel.getIsPlaying() ? playToPause : pauseToPlay;
        vPlayPauseBtn.setImageDrawable(drawable);
        drawable.start();
    }

    private void togglePlayPause() {
        final Intent playPauseIntent = new Intent(RadioService.PLAY_PAUSE);
        sendBroadcast(playPauseIntent);
    }

    private void favorite() {
        if (!App.getAuthUtil().isAuthenticated()) {
            showAuthActivity(MainActivity.LOGIN_FAVORITE_REQUEST);
            return;
        }

        final Intent favIntent = new Intent(RadioService.TOGGLE_FAVORITE);
        sendBroadcast(favIntent);
    }

    private void showHistory() {
        viewModel.toggleShowHistory();
    }

}
