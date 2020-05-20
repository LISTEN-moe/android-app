package me.echeung.moemoekyun.util.system

interface AudioManagerUtil {

    fun requestAudioFocus(): Int

    fun abandonAudioFocus()
}
