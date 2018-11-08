package me.echeung.moemoekyun.client.api.v4.response

import me.echeung.moemoekyun.client.model.Song

class SearchResponse : BaseResponse() {
    lateinit var songs: List<Song>
}
