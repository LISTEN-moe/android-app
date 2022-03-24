package me.echeung.moemoekyun.ui.activity

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.NonNull
import androidx.appcompat.widget.ActionMenuView
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import com.google.android.material.bottomsheet.BottomSheetBehavior
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.BR
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.client.api.Library
import me.echeung.moemoekyun.client.api.auth.AuthUtil
import me.echeung.moemoekyun.databinding.ActivityMainBinding
import me.echeung.moemoekyun.databinding.RadioControlsBinding
import me.echeung.moemoekyun.service.RadioService
import me.echeung.moemoekyun.ui.activity.auth.AuthActivityUtil
import me.echeung.moemoekyun.ui.activity.auth.AuthActivityUtil.broadcastAuthEvent
import me.echeung.moemoekyun.ui.activity.auth.AuthActivityUtil.handleAuthActivityResult
import me.echeung.moemoekyun.ui.activity.auth.AuthActivityUtil.showLoginActivity
import me.echeung.moemoekyun.ui.activity.auth.AuthActivityUtil.showLogoutDialog
import me.echeung.moemoekyun.ui.activity.auth.AuthActivityUtil.showRegisterActivity
import me.echeung.moemoekyun.ui.base.BaseActivity
import me.echeung.moemoekyun.ui.dialog.SleepTimerDialog
import me.echeung.moemoekyun.ui.view.PlayPauseView
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.SongActionsUtil
import me.echeung.moemoekyun.util.ext.openUrl
import me.echeung.moemoekyun.util.ext.startActivity
import me.echeung.moemoekyun.util.system.NetworkUtil
import me.echeung.moemoekyun.viewmodel.RadioViewModel
import me.echeung.moemoekyun.viewmodel.UserViewModel
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity() {

    private val radioViewModel: RadioViewModel by inject()
    private val userViewModel: UserViewModel by inject()

    private val radioClient: RadioClient by inject()

    private val authUtil: AuthUtil by inject()
    private val preferenceUtil: PreferenceUtil by inject()
    private val songActionsUtil: SongActionsUtil by inject()

    private lateinit var binding: ActivityMainBinding

    private var searchMenu: ActionMenuView? = null
    private var nowPlayingSheet: BottomSheetBehavior<*>? = null
    private var nowPlayingSheetMenu: Menu? = null

    private var playPauseCallback: Observable.OnPropertyChangedCallback? = null
    private var playPauseView: PlayPauseView? = null
    private var miniPlayPauseView: PlayPauseView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Replace splash screen theme
        setTheme(R.style.Theme_App)

        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.vm = radioViewModel

        binding.btnRetry.setOnClickListener { retry() }
        binding.btnLogin.setOnClickListener { showLoginActivity() }
        binding.btnRegister.setOnClickListener { showRegisterActivity() }

        // Check network connectivity
        if (!NetworkUtil.isNetworkAvailable(this)) {
            return
        }

        // Sets audio type to media (volume button control)
        volumeControlStream = AudioManager.STREAM_MUSIC

        initAppbar()
        initNowPlaying()
        initSecondaryMenus()

        // Invalidate token if needed
        val isAuthed = authUtil.checkAuthTokenValidity()
        radioViewModel.isAuthed = isAuthed
        if (!isAuthed) {
            userViewModel.reset()
        }
    }

    override fun onDestroy() {
        // Kill service/notification if killing activity and not playing
        val service = App.service
        if (service != null && !service.isPlaying) {
            sendBroadcast(Intent(RadioService.STOP).apply {
                setPackage(packageName)
            })
        }

        binding.unbind()

        if (playPauseCallback != null) {
            radioViewModel.removeOnPropertyChangedCallback(playPauseCallback!!)
        }

        super.onDestroy()
    }

    override fun onBackPressed() {
        // Collapse now playing
        if (nowPlayingSheet?.state == BottomSheetBehavior.STATE_EXPANDED) {
            nowPlayingSheet?.state = BottomSheetBehavior.STATE_COLLAPSED
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

            sendBroadcast(Intent(RadioService.UPDATE).apply {
                setPackage(packageName)
            })
        }
    }

    override fun initAppbar() {
        super.initAppbar()
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(false)
        }

        searchMenu = appbar?.findViewById(R.id.appbar_search_menu)
        searchMenu?.setOnMenuItemClickListener { menuItem -> onOptionsItemSelected(menuItem) }
    }

    private fun initNowPlaying() {
        nowPlayingSheet = BottomSheetBehavior.from(binding.nowPlaying.nowPlayingSheet)

        // Restore previous expanded state
        if (preferenceUtil.isNowPlayingExpanded().get()) {
            nowPlayingSheet?.setState(BottomSheetBehavior.STATE_EXPANDED)
        } else {
            radioViewModel.miniPlayerAlpha = 1f
        }

        nowPlayingSheet?.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
                preferenceUtil.isNowPlayingExpanded().set(newState == BottomSheetBehavior.STATE_EXPANDED)
            }

            override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {
                // Shows/hides mini player
                this@MainActivity.radioViewModel.miniPlayerAlpha = 1f - slideOffset
            }
        })

        // Expand when tap mini player
        binding.nowPlaying.miniPlayer.root.setOnClickListener { nowPlayingSheet!!.setState(BottomSheetBehavior.STATE_EXPANDED) }

        // Collapse button / when toolbar is tapped
        binding.nowPlaying.collapseBtn.setOnClickListener { nowPlayingSheet!!.setState(BottomSheetBehavior.STATE_COLLAPSED) }
        binding.nowPlaying.toolbar.setOnClickListener { nowPlayingSheet!!.setState(BottomSheetBehavior.STATE_COLLAPSED) }

        initPlayPause()

        val radioControls: RadioControlsBinding = binding.nowPlaying.fullPlayer.radioControls

        radioControls.historyBtn.setOnClickListener { showHistory() }
        radioControls.favoriteBtn.setOnClickListener { favorite() }

        // Press song info to show history
        val vCurrentSong = binding.nowPlaying.fullPlayer.radioSongs.currentSong
        vCurrentSong.setOnClickListener {
            if (radioViewModel.currentSong != null) {
                showHistory()
            }
        }

        // Long press song info to copy to clipboard
        vCurrentSong.setOnLongClickListener {
            songActionsUtil.copyToClipboard(this, radioViewModel.currentSong)
            true
        }

        // Long press album art to open in browser
        binding.nowPlaying.fullPlayer.radioAlbumArt.root.setOnLongClickListener {
            val currentSong = radioViewModel.currentSong ?: return@setOnLongClickListener false

            val albumArtUrl = currentSong.albumArtUrl ?: return@setOnLongClickListener false

            openUrl(albumArtUrl)
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        updateMenuOptions(menu)

        return true
    }

    override fun invalidateOptionsMenu() {
        super.invalidateOptionsMenu()
        nowPlayingSheetMenu?.let { updateMenuOptions(it) }
    }

    private fun initSecondaryMenus() {
        // Duplicate menu in now playing sheet
        val toolbar = binding.nowPlaying.toolbar
        toolbar.inflateMenu(R.menu.menu_main)
        nowPlayingSheetMenu = toolbar.menu

        toolbar.setOnMenuItemClickListener { this.onOptionsItemSelected(it) }

        updateMenuOptions(nowPlayingSheetMenu!!)

        // Secondary menu with search
        menuInflater.inflate(R.menu.menu_search, searchMenu?.menu)
    }

    private fun updateMenuOptions(menu: Menu) {
        // Toggle visibility of logout option based on authentication status
        menu.findItem(R.id.action_logout).isVisible = authUtil.isAuthenticated

        // Pre-check the library mode
        when (preferenceUtil.libraryMode().get()) {
            Library.jpop -> menu.findItem(R.id.action_library_jpop).isChecked = true
            Library.kpop -> menu.findItem(R.id.action_library_kpop).isChecked = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_library_jpop -> {
                item.isChecked = true
                setLibraryMode(Library.jpop)
                return true
            }

            R.id.action_library_kpop -> {
                item.isChecked = true
                setLibraryMode(Library.kpop)
                return true
            }

            R.id.action_logout -> {
                showLogoutDialog()
                return true
            }

            R.id.action_search -> {
                startActivity<SearchActivity>()
                return true
            }

            R.id.action_settings -> {
                startActivity<SettingsActivity>()
                return true
            }

            R.id.action_about -> {
                startActivity<AboutActivity>()
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
        handleAuthActivityResult(requestCode, resultCode, data)

        super.onActivityResult(requestCode, resultCode, data)
    }

    // Now playing stuff
    // =============================================================================================

    private fun initPlayPause() {
        val playPauseBtn = binding.nowPlaying.fullPlayer.radioControls.playPauseBtn
        playPauseBtn.setOnClickListener { togglePlayPause() }
        playPauseView = PlayPauseView(this, playPauseBtn)

        val miniPlayPauseBtn = binding.nowPlaying.miniPlayer.miniPlayPause
        miniPlayPauseBtn.setOnClickListener { togglePlayPause() }
        miniPlayPauseView = PlayPauseView(this, miniPlayPauseBtn)

        setPlayPauseDrawable()
        playPauseCallback = object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                if (propertyId == BR.playing || propertyId == BR.loading) {
                    setPlayPauseDrawable()
                }
            }
        }

        radioViewModel.addOnPropertyChangedCallback(playPauseCallback!!)
    }

    private fun setPlayPauseDrawable() {
        val isPlaying = radioViewModel.isPlaying
        val isLoading = radioViewModel.isLoading
        playPauseView?.toggle(isPlaying, isLoading)
        miniPlayPauseView?.toggle(isPlaying, isLoading)
    }

    private fun togglePlayPause() {
        sendBroadcast(Intent(RadioService.PLAY_PAUSE).apply {
            setPackage(packageName)
        })
    }

    private fun favorite() {
        if (!authUtil.isAuthenticated) {
            showLoginActivity(AuthActivityUtil.LOGIN_FAVORITE_REQUEST)
            return
        }

        sendBroadcast(Intent(RadioService.TOGGLE_FAVORITE).apply {
            setPackage(packageName)
        })
    }

    private fun showHistory() {
        songActionsUtil.showSongsDialog(this, getString(R.string.last_played), radioViewModel.history)
    }

    private fun setLibraryMode(libraryMode: Library) {
        radioClient.changeLibrary(libraryMode)
        broadcastAuthEvent()
        invalidateOptionsMenu()
    }
}
