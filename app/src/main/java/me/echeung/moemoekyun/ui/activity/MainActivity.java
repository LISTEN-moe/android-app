package me.echeung.moemoekyun.ui.activity;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.viewpager.widget.ViewPager;
import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.BR;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.adapter.ViewPagerAdapter;
import me.echeung.moemoekyun.client.api.library.Jpop;
import me.echeung.moemoekyun.client.api.library.Kpop;
import me.echeung.moemoekyun.client.model.Song;
import me.echeung.moemoekyun.databinding.ActivityMainBinding;
import me.echeung.moemoekyun.service.RadioService;
import me.echeung.moemoekyun.ui.base.BaseActivity;
import me.echeung.moemoekyun.ui.dialog.SleepTimerDialog;
import me.echeung.moemoekyun.ui.view.PlayPauseView;
import me.echeung.moemoekyun.util.SongActionsUtil;
import me.echeung.moemoekyun.util.system.NetworkUtil;
import me.echeung.moemoekyun.util.system.UrlUtil;
import me.echeung.moemoekyun.viewmodel.RadioViewModel;

public class MainActivity extends BaseActivity {

    public static final String AUTH_EVENT = "auth_event";

    public static final int LOGIN_REQUEST = 0;
    public static final int LOGIN_FAVORITE_REQUEST = 1;

    private ActivityMainBinding binding;

    private RadioViewModel viewModel;

    private ViewPager viewPager;
    private BottomSheetBehavior nowPlayingSheet;
    private Menu nowPlayingSheetMenu;

    private Observable.OnPropertyChangedCallback playPauseCallback;
    private PlayPauseView playPauseView;
    private PlayPauseView miniPlayPauseView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        viewModel = App.Companion.getRadioViewModel();
        binding.setVm(viewModel);

        binding.btnRetry.setOnClickListener(v -> retry());
        binding.btnLogin.setOnClickListener(v -> showLoginActivity());
        binding.btnRegister.setOnClickListener(v -> showRegisterActivity());

        // Check network connectivity
        if (!NetworkUtil.INSTANCE.isNetworkAvailable(this)) {
            return;
        }

        // Sets audio type to media (volume button control)
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Init app/tab bar
        initAppbar();

        // Init now playing sheet
        initNowPlaying();

        // Invalidate token if needed
        boolean isAuthed = App.Companion.getAuthUtil().checkAuthTokenValidity();
        viewModel.setAuthed(isAuthed);
        if (!isAuthed) {
            App.Companion.getUserViewModel().reset();
        }
    }

    @Override
    protected void onDestroy() {
        // Kill service/notification if killing activity and not playing
        RadioService service = App.Companion.getService();
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
        if (NetworkUtil.INSTANCE.isNetworkAvailable(this)) {
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

        initNowPlayingMenu();

        // Restore previous expanded state
        if (App.Companion.getPreferenceUtil().isNowPlayingExpanded()) {
            nowPlayingSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            viewModel.setMiniPlayerAlpha(1f);
        }

        nowPlayingSheet.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                App.Companion.getPreferenceUtil().setNowPlayingExpanded(newState == BottomSheetBehavior.STATE_EXPANDED);
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

        // Collapse button / when toolbar is tapped
        binding.nowPlaying.collapseBtn.setOnClickListener(v -> {
            nowPlayingSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
        binding.nowPlaying.toolbar.setOnClickListener(v -> {
            nowPlayingSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });

        // Clickable links
        binding.nowPlaying.content.radioControls.requestedBy.setMovementMethod(LinkMovementMethod.getInstance());

        initPlayPause();

        binding.nowPlaying.content.radioControls.historyBtn.setOnClickListener(v -> showHistory());
        binding.nowPlaying.content.radioControls.favoriteBtn.setOnClickListener(v -> favorite());

        // Press song info to show history
        LinearLayout vCurrentSong = binding.nowPlaying.content.radioSongs.currentSong;
        vCurrentSong.setOnClickListener(v -> {
            if (viewModel.getCurrentSong() != null) {
                showHistory();
            }
        });

        // Long press song info to copy to clipboard
        vCurrentSong.setOnLongClickListener(v -> {
            SongActionsUtil.INSTANCE.copyToClipboard(this, viewModel.getCurrentSong());
            return true;
        });

        // Long press album art to open in browser
        binding.nowPlaying.content.radioAlbumArt.getRoot().setOnLongClickListener(v -> {
            Song currentSong = viewModel.getCurrentSong();
            if (currentSong == null) {
                return false;
            }
            String albumArtUrl = currentSong.getAlbumArtUrl();
            if (albumArtUrl == null) {
                return false;
            }

            UrlUtil.INSTANCE.open(this, albumArtUrl);
            return true;
        });
    }

    private void initNowPlayingMenu() {
        Toolbar toolbar = binding.nowPlaying.toolbar;
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);

        nowPlayingSheetMenu = toolbar.getMenu();
        updateMenuOptions(nowPlayingSheetMenu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        updateMenuOptions(menu);

        return true;
    }

    @Override
    public void invalidateOptionsMenu() {
        super.invalidateOptionsMenu();
        updateMenuOptions(nowPlayingSheetMenu);
    }

    private void updateMenuOptions(Menu menu) {
        // Toggle visibility of logout option based on authentication status
        menu.findItem(R.id.action_logout).setVisible(App.Companion.getAuthUtil().isAuthenticated());

        // Pre-check the library mode
        switch (App.Companion.getPreferenceUtil().getLibraryMode()) {
            case Jpop.NAME:
                menu.findItem(R.id.action_library_jpop).setChecked(true);
                break;

            case Kpop.NAME:
                menu.findItem(R.id.action_library_kpop).setChecked(true);
                break;
        }
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
        App.Companion.getAuthViewModel().setShowRegister(false);
        startActivityForResult(new Intent(this, AuthActivity.class), requestCode);
    }

    private void broadcastAuthEvent() {
        sendBroadcast(new Intent(MainActivity.AUTH_EVENT));

        viewModel.setAuthed(App.Companion.getAuthUtil().isAuthenticated());
    }

    private void showRegisterActivity() {
        App.Companion.getAuthViewModel().setShowRegister(true);
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
        if (!App.Companion.getAuthUtil().isAuthenticated()) {
            return;
        }

        App.Companion.getAuthUtil().clearAuthToken();
        App.Companion.getUserViewModel().reset();

        Toast.makeText(getApplicationContext(), getString(R.string.logged_out), Toast.LENGTH_LONG).show();
        invalidateOptionsMenu();

        broadcastAuthEvent();
    }


    // Now playing stuff
    // =============================================================================================

    private void initPlayPause() {
        ImageView playPauseBtn = binding.nowPlaying.content.radioControls.playPauseBtn;
        playPauseBtn.setOnClickListener(v -> togglePlayPause());
        playPauseView = new PlayPauseView(this, playPauseBtn);

        ImageView miniPlayPauseBtn = binding.nowPlaying.miniPlayPause;
        miniPlayPauseBtn.setOnClickListener(v -> togglePlayPause());
        miniPlayPauseView = new PlayPauseView(this, miniPlayPauseBtn);

        setPlayPauseDrawable();
        playPauseCallback = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (propertyId == BR.playing) {
                    setPlayPauseDrawable();
                }
            }
        };

        viewModel.addOnPropertyChangedCallback(playPauseCallback);
    }

    private void setPlayPauseDrawable() {
        boolean isPlaying = viewModel.isPlaying();
        playPauseView.toggle(isPlaying);
        miniPlayPauseView.toggle(isPlaying);
    }

    private void togglePlayPause() {
        sendBroadcast(new Intent(RadioService.PLAY_PAUSE));
    }

    private void favorite() {
        if (!App.Companion.getAuthUtil().isAuthenticated()) {
            showLoginActivity(MainActivity.LOGIN_FAVORITE_REQUEST);
            return;
        }

        sendBroadcast(new Intent(RadioService.TOGGLE_FAVORITE));
    }

    private void showHistory() {
        SongActionsUtil.INSTANCE.showSongsDialog(this, getString(R.string.last_played), viewModel.getHistory());
    }

    private void setLibraryMode(String libraryMode) {
        App.Companion.getPreferenceUtil().setLibraryMode(libraryMode);
        broadcastAuthEvent();
        invalidateOptionsMenu();
    }

}
