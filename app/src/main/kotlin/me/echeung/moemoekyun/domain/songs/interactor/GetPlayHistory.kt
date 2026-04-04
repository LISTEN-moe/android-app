package me.echeung.moemoekyun.domain.songs.interactor

import me.echeung.moemoekyun.client.api.ApiClient
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.domain.songs.model.SongConverter
import javax.inject.Inject

class GetPlayHistory @Inject constructor(private val api: ApiClient, private val songConverter: SongConverter) {
    suspend fun await(kpop: Boolean): List<DomainSong> = api.getPlayHistory(kpop).map(songConverter::toDomainSong)
}
