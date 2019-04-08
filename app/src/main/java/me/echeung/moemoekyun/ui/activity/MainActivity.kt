package me.echeung.moemoekyun.ui.activity

import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.ActionMenuView
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.BR
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.adapter.ViewPagerAdapter
import me.echeung.moemoekyun.client.api.v4.library.Jpop
import me.echeung.moemoekyun.client.api.v4.library.Kpop
import me.echeung.moemoekyun.databinding.ActivityMainBinding
import me.echeung.moemoekyun.databinding.RadioControlsBinding
import me.echeung.moemoekyun.service.RadioService
import me.echeung.moemoekyun.ui.base.BaseActivity
import me.echeung.moemoekyun.ui.dialog.SleepTimerDialog
import me.echeung.moemoekyun.ui.view.PlayPauseView
import me.echeung.moemoekyun.util.SongActionsUtil
import me.echeung.moemoekyun.util.system.NetworkUtil
import me.echeung.moemoekyun.util.system.openUrl
import me.echeung.moemoekyun.util.system.startActivity
import me.echeung.moemoekyun.util.system.toast
import me.echeung.moemoekyun.viewmodel.RadioViewModel


class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: RadioViewModel

    private var viewPager: ViewPager? = null
    private var searchMenu: ActionMenuView? = null
    private var nowPlayingSheet: BottomSheetBehavior<*>? = null
    private var nowPlayingSheetMenu: Menu? = null

    private var playPauseCallback: Observable.OnPropertyChangedCallback? = null
    private var playPauseView: PlayPauseView? = null
    private var miniPlayPauseView: PlayPauseView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        viewModel = App.radioViewModel!!
        binding.vm = viewModel

        binding.btnRetry.setOnClickListener { retry() }
        binding.btnLogin.setOnClickListener { showLoginActivity() }
        binding.btnRegister.setOnClickListener { showRegisterActivity() }

        // Check network connectivity
        if (!NetworkUtil.isNetworkAvailable(this)) {
            return
        }

        // Sets audio type to media (volume button control)
        volumeControlStream = AudioManager.STREAM_MUSIC

        // Init app/tab bar
        initAppbar()

        // Init now playing sheet
        initNowPlaying()

        // Invalidate token if needed
        val isAuthed = App.authUtil.checkAuthTokenValidity()
        viewModel.isAuthed = isAuthed
        if (!isAuthed) {
            App.userViewModel!!.reset()
        }
    }

    override fun onDestroy() {
        // Kill service/notification if killing activity and not playing
        val service = App.service
        if (service != null && !service.isPlaying) {
            sendBroadcast(Intent(RadioService.STOP))
        }

        if (viewPager != null) {
            viewPager!!.adapter = null
        }

        binding.unbind()

        if (playPauseCallback != null) {
            viewModel.removeOnPropertyChangedCallback(playPauseCallback!!)
        }

        super.onDestroy()
    }

    override fun onBackPressed() {
        // Collapse now playing
        if (nowPlayingSheet?.state == BottomSheetBehavior.STATE_EXPANDED) {
            nowPlayingSheet!!.state = BottomSheetBehavior.STATE_COLLAPSED
            return
        }

        super.onBackPressed()
    }

    /**
     * For retry button in no internet view.
     */
    private fun retry() {
        if (NetworkUtil.isNetworkAvailable(this)) {
            recreate()

            sendBroadcast(Intent(RadioService.UPDATE))
        }
    }

    /**
     * Initializes everything for the tabs: the adapter, icons, and title handler
     */
    private fun initAppbar() {
        // Set up app bar
        val appbar = binding.appbar
        setSupportActionBar(appbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        searchMenu = appbar.findViewById(R.id.appbar_search_menu)
        searchMenu!!.setOnMenuItemClickListener(object : ActionMenuView.OnMenuItemClickListener {
            override fun onMenuItemClick(menuItem: MenuItem): Boolean {
                return onOptionsItemSelected(menuItem)
            }
        })

        // Set up ViewPager and adapter
        viewPager = binding.pager
        val mViewPagerAdapter = ViewPagerAdapter(this, supportFragmentManager)
        viewPager!!.adapter = mViewPagerAdapter

        // Set up tabs
        val tabLayout = binding.tabs
        tabLayout.setupWithViewPager(viewPager)
        viewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        for (i in 0 until tabLayout.tabCount) {
            tabLayout.getTabAt(i)!!.setIcon(mViewPagerAdapter.getIcon(i))
        }
    }

    private fun initNowPlaying() {
        nowPlayingSheet = BottomSheetBehavior.from(binding.nowPlaying.nowPlayingSheet)

        initNowPlayingMenu()

        // Restore previous expanded state
        if (App.preferenceUtil!!.isNowPlayingExpanded) {
            nowPlayingSheet!!.setState(BottomSheetBehavior.STATE_EXPANDED)
        } else {
            viewModel.miniPlayerAlpha = 1f
        }

        nowPlayingSheet!!.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
                App.preferenceUtil!!.isNowPlayingExpanded = newState == BottomSheetBehavior.STATE_EXPANDED
            }

            override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {
                // Shows/hides mini player
                viewModel.miniPlayerAlpha = 1f - slideOffset
            }
        })

        // Expand when tap mini player
        binding.nowPlaying.miniPlayer.setOnClickListener { nowPlayingSheet!!.setState(BottomSheetBehavior.STATE_EXPANDED) }

        // Collapse button / when toolbar is tapped
        binding.nowPlaying.collapseBtn.setOnClickListener { nowPlayingSheet!!.setState(BottomSheetBehavior.STATE_COLLAPSED) }
        binding.nowPlaying.toolbar.setOnClickListener { nowPlayingSheet!!.setState(BottomSheetBehavior.STATE_COLLAPSED) }

        initPlayPause()

        val radioControls: RadioControlsBinding = binding.nowPlaying.radioControls

        radioControls.historyBtn.setOnClickListener { showHistory() }
        radioControls.favoriteBtn.setOnClickListener { favorite() }

        // Press song info to show history
        val vCurrentSong = binding.nowPlaying.radioSongs.currentSong
        vCurrentSong.setOnClickListener {
            if (viewModel.currentSong != null) {
                showHistory()
            }
        }

        // Long press song info to copy to clipboard
        vCurrentSong.setOnLongClickListener {
            SongActionsUtil.copyToClipboard(this, viewModel.currentSong)
            true
        }

        // Long press album art to open in browser
        binding.nowPlaying.radioAlbumArt.root.setOnLongClickListener {
            val currentSong = viewModel.currentSong ?: return@setOnLongClickListener false

            val albumArtUrl = currentSong.albumArtUrl ?: return@setOnLongClickListener false

            openUrl(albumArtUrl)
            true
        }
    }

    private fun initNowPlayingMenu() {
        val toolbar = binding.nowPlaying.toolbar
        toolbar.inflateMenu(R.menu.menu_main)
        toolbar.setOnMenuItemClickListener { this.onOptionsItemSelected(it) }

        nowPlayingSheetMenu = toolbar.menu
        updateMenuOptions(nowPlayingSheetMenu!!)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        updateMenuOptions(menu)

        // Secondary menu with search
        menuInflater.inflate(R.menu.menu_search, searchMenu!!.menu)

        return true
    }

    override fun invalidateOptionsMenu() {
        super.invalidateOptionsMenu()
        updateMenuOptions(nowPlayingSheetMenu!!)
    }

    private fun updateMenuOptions(menu: Menu) {
        // Toggle visibility of logout option based on authentication status
        menu.findItem(R.id.action_logout).isVisible = App.authUtil.isAuthenticated

        // Pre-check the library mode
        when (App.preferenceUtil!!.libraryMode) {
            Jpop.NAME -> menu.findItem(R.id.action_library_jpop).isChecked = true

            Kpop.NAME -> menu.findItem(R.id.action_library_kpop).isChecked = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_library_jpop -> {
                item.isChecked = true
                setLibraryMode(Jpop.NAME)
                return true
            }

            R.id.action_library_kpop -> {
                item.isChecked = true
                setLibraryMode(Kpop.NAME)
                return true
            }

            R.id.action_logout -> {
                showLogoutDialog()
                return true
            }

            R.id.action_search -> {
                startActivity<SearchActivity>(this)
                return true
            }

            R.id.action_settings -> {
                startActivity<SettingsActivity>(this)
                return true
            }

            R.id.action_about -> {
                startActivity<AboutActivity>(this)
                return true
            }

            R.id.action_sleep_timer -> {
                SleepTimerDialog(this)
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    // Auth stuff
    // =============================================================================================

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        invalidateOptionsMenu()
        broadcastAuthEvent()

        when (requestCode) {
            LOGIN_FAVORITE_REQUEST -> sendBroadcast(Intent(RadioService.TOGGLE_FAVORITE))
        }
    }

    @JvmOverloads
    fun showLoginActivity(requestCode: Int = LOGIN_REQUEST) {
        startActivityForResult(Intent(this, AuthLoginActivity::class.java), requestCode)
    }

    private fun broadcastAuthEvent() {
        sendBroadcast(Intent(MainActivity.AUTH_EVENT))

        viewModel.isAuthed = App.authUtil.isAuthenticated
    }

    private fun showRegisterActivity() {
        startActivity<AuthRegisterActivity>(this)
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle(R.string.logout)
                .setMessage(getString(R.string.logout_confirmation))
                .setPositiveButton(R.string.logout) { _, _ -> logout() }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show()
    }

    private fun logout() {
        if (!App.authUtil.isAuthenticated) {
            return
        }

        App.authUtil.clearAuthToken()
        App.userViewModel!!.reset()

        applicationContext.toast(getString(R.string.logged_out), Toast.LENGTH_LONG)
        invalidateOptionsMenu()

        broadcastAuthEvent()
    }

    // Now playing stuff
    // =============================================================================================

    private fun initPlayPause() {
        val playPauseBtn = binding.nowPlaying.radioControls.playPauseBtn
        playPauseBtn.setOnClickListener { togglePlayPause() }
        playPauseView = PlayPauseView(this, playPauseBtn)

        val miniPlayPauseBtn = binding.nowPlaying.miniPlayPause
        miniPlayPauseBtn.setOnClickListener { togglePlayPause() }
        miniPlayPauseView = PlayPauseView(this, miniPlayPauseBtn)

        setPlayPauseDrawable()
        playPauseCallback = object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                if (propertyId == BR.playing) {
                    setPlayPauseDrawable()
                }
            }
        }

        viewModel.addOnPropertyChangedCallback(playPauseCallback!!)
    }

    private fun setPlayPauseDrawable() {
        val isPlaying = viewModel.isPlaying
        playPauseView!!.toggle(isPlaying)
        miniPlayPauseView!!.toggle(isPlaying)
    }

    private fun togglePlayPause() {
        sendBroadcast(Intent(RadioService.PLAY_PAUSE))
    }

    private fun favorite() {
        if (!App.authUtil.isAuthenticated) {
            showLoginActivity(MainActivity.LOGIN_FAVORITE_REQUEST)
            return
        }

        sendBroadcast(Intent(RadioService.TOGGLE_FAVORITE))
    }

    private fun showHistory() {
        SongActionsUtil.showSongsDialog(this, getString(R.string.last_played), viewModel.history)
    }

    private fun setLibraryMode(libraryMode: String) {
        App.preferenceUtil!!.libraryMode = libraryMode
        broadcastAuthEvent()
        invalidateOptionsMenu()
    }

    companion object {
        private const val LOGIN_REQUEST = 0
        private const val LOGIN_FAVORITE_REQUEST = 1

        const val AUTH_EVENT = "auth_event"
    }
}
