package me.echeung.moemoekyun.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.android.databinding.library.baseAdapters.BR;

public class SearchBarViewModel extends BaseObservable {

    private String query;

    @Bindable
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
        notifyPropertyChanged(BR.query);
    }

    public void clearQuery() {
        this.query = null;
        notifyPropertyChanged(BR.query);
    }

}
