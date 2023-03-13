package me.echeung.moemoekyun.domain.radio.interactor

import me.echeung.moemoekyun.client.api.Station
import me.echeung.moemoekyun.domain.radio.RadioService
import javax.inject.Inject

class SetStation @Inject constructor(
    private val radioService: RadioService,
) {

    fun set(station: Station) {
        radioService.setStation(station)
    }
}
