package me.echeung.moemoekyun.viewmodel

import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import me.echeung.moemoekyun.client.model.User

class UserViewModel : BaseViewModel() {

    @get:Bindable
    var user: User? = null
        set(user) {
            field = user
            requestsRemaining = user?.requestsRemaining ?: 0
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

    var requestsRemaining: Int
        @Bindable
        get() = if (this.user == null) {
            0
        } else this.user!!.requestsRemaining
        set(requestsRemaining) {
            if (this.user != null) {
                this.user!!.requestsRemaining = requestsRemaining
            }
            notifyPropertyChanged(BR.requestsRemaining)
        }

    fun reset() {
        user = null
        avatarUrl = null
        bannerUrl = null

        requestsRemaining = 0
        hasFavorites = false
    }

}
