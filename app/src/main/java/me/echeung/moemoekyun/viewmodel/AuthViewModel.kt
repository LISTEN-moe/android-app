package me.echeung.moemoekyun.viewmodel

import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR

class AuthViewModel : BaseViewModel() {

    @get:Bindable
    var showRegister = false
        set(showRegister) {
            field = showRegister
            notifyPropertyChanged(BR.showRegister)
        }

}
