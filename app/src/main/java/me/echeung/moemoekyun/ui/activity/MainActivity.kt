package me.echeung.moemoekyun.ui.activity

import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.NonNull
import androidx.appcompat.widget.ActionMenuView
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.common.images.WebImage
import com.google.android.material.bottomsheet.BottomSheetBehavior
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.BR
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.cast.CastDelegate
import me.echeung.moemoekyun.client.api.library.Jpop
import me.echeung.moemoekyun.client.api.library.Kpop
import me.echeung.moemoekyun.databinding.ActivityMainBinding
import me.echeung.moemoekyun.databinding.RadioControlsBinding
import me.echeung.moemoekyun.service.RadioService
import me.echeung.moemoekyun.ui.base.BaseActivity
import me.echeung.moemoekyun.ui.dialog.SleepTimerDialog
import me.echeung.moemoekyun.ui.view.PlayPauseView
import me.echeung.moemoekyun.util.AuthActivityUtil
import me.echeung.moemoekyun.util.AuthActivityUtil.broadcastAuthEvent
import me.echeung.moemoekyun.util.AuthActivityUtil.handleAuthActivityResult
import me.echeung.moemoekyun.util.AuthActivityUtil.showLoginActivity
import me.echeung.moemoekyun.util.AuthActivityUtil.showLogoutDialog
import me.echeung.moemoekyun.util.AuthActivityUtil.showRegisterActivity
import me.echeung.moemoekyun.util.SongActionsUtil
import me.echeung.moemoekyun.util.system.NetworkUtil
import me.echeung.moemoekyun.util.system.openUrl
import me.echeung.moemoekyun.util.system.startActivity
import me.echeung.moemoekyun.viewmodel.RadioViewModel

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: RadioViewModel

    private var searchMenu: ActionMenuView? = null
    private var nowPlayingSheet: BottomSheetBehavior<*>? = null
    private var nowPlayingSheetMenu: Menu? = null

    private var playPauseCallback: Observable.OnPropertyChangedCallback? = null
    private var playPauseView: PlayPauseView? = null
    private var miniPlayPauseView: PlayPauseView? = null

    private var castPlayer: CastPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Replace splash screen theme
        setTheme(R.style.AppTheme)

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

        initAppbar()
        initMenu()
        initNowPlaying()

        // Invalidate token if needed
        val isAuthed = App.authTokenUtil.checkAuthTokenValidity()
        viewModel.isAuthed = isAuthed
        if (!isAuthed) {
            App.userViewModel!!.reset()
        }

        // Google Cast
        CastDelegate().init(this)
    }

    override fun onDestroy() {
        // Kill service/notification if killing activity and not playing
        val service = App.service
        if (service != null && !service.isPlaying) {
            sendBroadcast(Intent(RadioService.STOP))
        }

        binding.unbind()

        if (playPauseCallback != null) {
            viewModel.removeOnPropertyChangedCallback(playPauseCallback!!)
        }

        castPlayer?.release()

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

    override fun initAppbar() {
        super.initAppbar()
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        searchMenu = appbar?.findViewById(R.id.appbar_search_menu)
        searchMenu!!.setOnMenuItemClickListener(object : ActionMenuView.OnMenuItemClickListener {
            override fun onMenuItemClick(menuItem: MenuItem): Boolean {
                return onOptionsItemSelected(menuItem)
            }
        })
    }

    private fun initNowPlaying() {
        nowPlayingSheet = BottomSheetBehavior.from(binding.nowPlaying.nowPlayingSheet)

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

    private fun initMenu() {
        val toolbar = binding.nowPlaying.toolbar
        toolbar.inflateMenu(R.menu.menu_main)
        nowPlayingSheetMenu = toolbar.menu

        CastButtonFactory.setUpMediaRouteButton(
                applicationContext,
                nowPlayingSheetMenu,
                R.id.media_route_menu_item)

        toolbar.setOnMenuItemClickListener { this.onOptionsItemSelected(it) }

        updateMenuOptions(nowPlayingSheetMenu!!)

        // Secondary menu with search
        menuInflater.inflate(R.menu.menu_search, searchMenu!!.menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        CastButtonFactory.setUpMediaRouteButton(
                applicationContext,
                menu,
                R.id.media_route_menu_item)

        updateMenuOptions(menu)

        return true
    }

    override fun invalidateOptionsMenu() {
        super.invalidateOptionsMenu()
        updateMenuOptions(nowPlayingSheetMenu!!)
    }

    private fun updateMenuOptions(menu: Menu) {
        // Toggle visibility of logout option based on authentication status
        menu.findItem(R.id.action_logout).isVisible = App.authTokenUtil.isAuthenticated

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
        handleAuthActivityResult(requestCode, resultCode, data)
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
        if (!App.authTokenUtil.isAuthenticated) {
            showLoginActivity(AuthActivityUtil.LOGIN_FAVORITE_REQUEST)
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
}
