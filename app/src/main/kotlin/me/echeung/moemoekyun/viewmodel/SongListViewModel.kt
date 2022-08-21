package me.echeung.moemoekyun.viewmodel

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR

class SongListViewModel : BaseObservable() {

    @get:Bindable
    var query: String? = null
        set(query) {
            field = query
            notifyPropertyChanged(BR.query)
        }

    @get:Bindable
    var hasResults: Boolean = false
        set(hasResults) {
            field = hasResults
            notifyPropertyChanged(BR.hasResults)
        }

    fun clearQuery() {
        query = null
    }
}
