package me.echeung.moemoekyun.ui.screen.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import me.echeung.moemoekyun.util.PreferenceUtil
import javax.inject.Inject

@HiltViewModel
class SettingsScreenModel @Inject constructor(val preferenceUtil: PreferenceUtil) : ViewModel()
