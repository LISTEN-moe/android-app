package me.echeung.moemoekyun.client.api.v4.response

import me.echeung.moemoekyun.client.model.SongListItem

class SongsResponse : BaseResponse() {
    lateinit var songs: List<SongListItem>
}
