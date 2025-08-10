package me.echeung.moemoekyun.domain.radio.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import me.echeung.moemoekyun.domain.radio.RadioService
import javax.inject.Inject
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class CurrentSong @Inject constructor(private val radioService: RadioService) {

    fun albumArtFlow(): Flow<String?> = radioService.state
        .map { it.albumArtUrl }
        .distinctUntilChanged()

    @OptIn(ExperimentalTime::class)
    fun songProgressFlow(): Flow<Pair<Instant?, Long?>> = radioService.state
        .map { Pair(it.startTime, it.currentSong?.durationSeconds) }
        .distinctUntilChanged()
}
