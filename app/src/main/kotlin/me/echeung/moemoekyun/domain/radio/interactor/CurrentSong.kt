package me.echeung.moemoekyun.domain.radio.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import me.echeung.moemoekyun.domain.radio.RadioService
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import javax.inject.Inject

class CurrentSong @Inject constructor(
    private val radioService: RadioService,
) {

    fun asFlow(): Flow<DomainSong?> {
        return radioService.state
            .map { it.currentSong }
            .distinctUntilChanged()
    }
}
