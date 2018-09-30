package me.echeung.moemoekyun.client.api.response

import me.echeung.moemoekyun.client.model.SongListItem

class SongsResponse : BaseResponse() {
    lateinit var songs: List<SongListItem>
}
