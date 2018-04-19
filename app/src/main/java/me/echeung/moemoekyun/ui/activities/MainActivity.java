package me.echeung.moemoekyun.ui.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.BR;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.adapters.ViewPagerAdapter;
import me.echeung.moemoekyun.api.endpoints.Jpop;
import me.echeung.moemoekyun.api.endpoints.Kpop;
import me.echeung.moemoekyun.databinding.ActivityMainBinding;
import me.echeung.moemoekyun.service.RadioService;
import me.echeung.moemoekyun.ui.dialogs.SleepTimerDialog;
import me.echeung.moemoekyun.utils.DozeUtil;
import me.echeung.moemoekyun.utils.NetworkUtil;
import me.echeung.moemoekyun.utils.SongActionsUtil;
import me.echeung.moemoekyun.viewmodels.RadioViewModel;

public class MainActivity extends BaseActivity {

    public static final String AUTH_EVENT = "auth_event";

    public static final int LOGIN_REQUEST = 0;
    public static final int LOGIN_FAVORITE_REQUEST = 1;

    private ActivityMainBinding binding;

    private RadioViewModel viewModel;

    private ViewPager viewPager;
    private BottomSheetBehavior nowPlayingSheet;

    private Observable.OnPropertyChangedCallback playPauseCallback;
    private FloatingActionButton vPlayPauseBtn;
    private ImageButton vMiniPlayPauseBtn;
    private AnimatedVectorDrawable playToPause;
    private AnimatedVectorDrawable pauseToPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        viewModel = App.getRadioViewModel();
        binding.setVm(viewModel);

        binding.btnRetry.setOnClickListener(v -> retry());
        binding.btnLogin.setOnClickListener(v -> showLoginActivity());
        binding.btnRegister.setOnClickListener(v -> showRegisterActivity());

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

        // Prompt to turn off battery optimizations
        if (!DozeUtil.isWhitelisted(this)) {
            DozeUtil.requestWhitelist(this);
        }
    }

    @Override
    protected void onDestroy() {
        // Kill service/notification if killing activity and not playing
        RadioService service = App.getService();
        if (service != null && !service.isPlaying()) {
            sendBroadcast(new Intent(RadioService.STOP));
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
        // Collapse now playing
        if (nowPlayingSheet != null && nowPlayingSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            nowPlayingSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return;
        }

        // Go back to first tab
        if (viewPager != null && viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem(0, true);
            return;
        }

        super.onBackPressed();
    }

    /**
     * For retry button in no internet view.
     */
    private void retry() {
        if (NetworkUtil.isNetworkAvailable(this)) {
            recreate();

            sendBroadcast(new Intent(RadioService.UPDATE));
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
        ViewPagerAdapter mViewPagerAdapter = new ViewPagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(mViewPagerAdapter);

        // Set up tabs
        TabLayout tabLayout = binding.tabs;
        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setIcon(mViewPagerAdapter.getIcon(i));
        }
    }

    private void initNowPlaying() {
        nowPlayingSheet = BottomSheetBehavior.from(binding.nowPlaying.nowPlayingSheet);

        // Restore previous expanded state
        if (App.getPreferenceUtil().isNowPlayingExpanded()) {
            nowPlayingSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            viewModel.setMiniPlayerAlpha(1f);
        }

        nowPlayingSheet.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                App.getPreferenceUtil().setIsNowPlayingExpanded(newState == BottomSheetBehavior.STATE_EXPANDED);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Shows/hides mini player
                viewModel.setMiniPlayerAlpha(1f - slideOffset);
            }
        });

        // Expand when tap mini player
        binding.nowPlaying.miniPlayer.setOnClickListener(v -> {
            nowPlayingSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        // Collapse button
        binding.nowPlaying.collapseBtn.setOnClickListener(v -> {
            nowPlayingSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });

        // Clickable links
        binding.nowPlaying.content.radioControls.requestedBy.setMovementMethod(LinkMovementMethod.getInstance());

        initPlayPause();

        binding.nowPlaying.content.radioControls.historyBtn.setOnClickListener(v -> showHistory());
        binding.nowPlaying.content.radioControls.favoriteBtn.setOnClickListener(v -> favorite());

        LinearLayout vCurrentSong = binding.nowPlaying.content.radioSongs.currentSong;
        vCurrentSong.setOnClickListener(v -> showHistory());
        vCurrentSong.setOnLongClickListener(v -> {
            SongActionsUtil.copyToClipboard(this, viewModel.getCurrentSong());
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Toggle visibility of logout option based on authentication status
        menu.findItem(R.id.action_logout).setVisible(App.getAuthUtil().isAuthenticated());

        // Pre-check the library mode
        switch (App.getPreferenceUtil().getLibraryMode()) {
            case Jpop.NAME:
                menu.findItem(R.id.action_library_jpop).setChecked(true);
                break;

            case Kpop.NAME:
                menu.findItem(R.id.action_library_kpop).setChecked(true);
                break;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_library_jpop:
                item.setChecked(true);
                setLibraryMode(Jpop.NAME);
                return true;

            case R.id.action_library_kpop:
                item.setChecked(true);
                setLibraryMode(Kpop.NAME);
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
                sendBroadcast(new Intent(RadioService.TOGGLE_FAVORITE));
                break;
        }
    }

    public void showLoginActivity() {
        showLoginActivity(LOGIN_REQUEST);
    }

    public void showLoginActivity(int requestCode) {
        App.getAuthViewModel().setShowRegister(false);
        startActivityForResult(new Intent(this, AuthActivity.class), requestCode);
    }

    private void broadcastAuthEvent() {
        sendBroadcast(new Intent(MainActivity.AUTH_EVENT));

        viewModel.setIsAuthed(App.getAuthUtil().isAuthenticated());
    }

    private void showRegisterActivity() {
        App.getAuthViewModel().setShowRegister(true);
        startActivity(new Intent(this, AuthActivity.class));
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
        vPlayPauseBtn = binding.nowPlaying.content.radioControls.playPauseBtn;
        vPlayPauseBtn.setOnClickListener(v -> togglePlayPause());

        vMiniPlayPauseBtn = binding.nowPlaying.miniPlayPause;
        vMiniPlayPauseBtn.setOnClickListener(v -> togglePlayPause());

        playToPause = (AnimatedVectorDrawable) ContextCompat.getDrawable(this, R.drawable.avd_play_to_pause);
        pauseToPlay = (AnimatedVectorDrawable) ContextCompat.getDrawable(this, R.drawable.avd_pause_to_play);

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
        AnimatedVectorDrawable drawable = viewModel.getIsPlaying() ? playToPause : pauseToPlay;
        vPlayPauseBtn.setImageDrawable(drawable);
        vMiniPlayPauseBtn.setImageDrawable(drawable);
        drawable.start();
    }

    private void togglePlayPause() {
        sendBroadcast(new Intent(RadioService.PLAY_PAUSE));
    }

    private void favorite() {
        if (!App.getAuthUtil().isAuthenticated()) {
            showLoginActivity(MainActivity.LOGIN_FAVORITE_REQUEST);
            return;
        }

        sendBroadcast(new Intent(RadioService.TOGGLE_FAVORITE));
    }

    private void showHistory() {
        SongActionsUtil.showSongsDialog(this, getString(R.string.last_played), viewModel.getHistory());
    }

    private void setLibraryMode(String libraryMode) {
        App.getPreferenceUtil().setLibraryMode(libraryMode);
        broadcastAuthEvent();
    }

}
