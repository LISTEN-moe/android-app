package me.echeung.moemoekyun.client.api.response

import me.echeung.moemoekyun.client.model.Song

class FavoritesResponse : BaseResponse() {
    lateinit var favorites: List<Song>
}
