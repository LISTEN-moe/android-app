package me.echeung.moemoekyun.domain.radio.interactor

import me.echeung.moemoekyun.domain.radio.RadioService
import javax.inject.Inject

class PlayPause @Inject constructor(
    private val radioService: RadioService,
) {

    fun play() {
        radioService.play()
    }

    fun pause() {
        radioService.pause()
    }

    fun stop() {
        radioService.stop()
    }

    fun toggle() {
        radioService.togglePlayState()
    }
}
