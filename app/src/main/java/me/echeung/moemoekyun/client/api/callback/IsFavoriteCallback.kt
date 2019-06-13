package me.echeung.moemoekyun.client.api.callback

interface IsFavoriteCallback : BaseCallback {
    fun onSuccess(favoritedSongIds: List<Int>)
}
