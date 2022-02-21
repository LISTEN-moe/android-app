package me.echeung.moemoekyun.viewmodel

import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import me.echeung.moemoekyun.client.api.model.User

class UserViewModel : BaseViewModel() {

    @get:Bindable
    var user: User? = null
        set(user) {
            field = user
            notifyPropertyChanged(BR.user)
        }

    @get:Bindable
    var avatarUrl: String? = null
        set(avatarUrl) {
            field = avatarUrl
            notifyPropertyChanged(BR.avatarUrl)
        }

    @get:Bindable
    var bannerUrl: String? = null
        set(bannerUrl) {
            field = bannerUrl
            notifyPropertyChanged(BR.bannerUrl)
        }

    @get:Bindable
    var hasFavorites: Boolean = false
        set(hasFavorites) {
            field = hasFavorites
            notifyPropertyChanged(BR.hasFavorites)
        }

    fun reset() {
        user = null
        avatarUrl = null
        bannerUrl = null

        hasFavorites = false
    }
}
