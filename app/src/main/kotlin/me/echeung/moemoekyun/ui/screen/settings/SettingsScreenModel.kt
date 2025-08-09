package me.echeung.moemoekyun.ui.screen.settings

import cafe.adriel.voyager.core.model.ScreenModel
import me.echeung.moemoekyun.util.PreferenceUtil
import javax.inject.Inject

class SettingsScreenModel @Inject constructor(val preferenceUtil: PreferenceUtil) : ScreenModel
