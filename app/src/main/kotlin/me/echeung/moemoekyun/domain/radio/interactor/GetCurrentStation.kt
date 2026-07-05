package me.echeung.moemoekyun.domain.radio.interactor

import me.echeung.moemoekyun.client.api.Station
import me.echeung.moemoekyun.util.PreferenceUtil
import javax.inject.Inject

/**
 * Reads the currently selected radio station. [PreferenceUtil] is the single source of
 * truth for the station, so this wraps it and offers the common conversions callers need
 * (the GraphQL `kpop` flag and the REST station param) in one place.
 */
class GetCurrentStation @Inject constructor(private val preferenceUtil: PreferenceUtil) {

    fun get(): Station = preferenceUtil.station().get()

    /** GraphQL queries take a nullable `kpop` boolean. */
    fun isKpop(): Boolean = get().isKpop

    /** REST endpoints (search, charts) expect a "jpop"/"kpop" station param. */
    fun apiParam(): String = get().apiParam
}
