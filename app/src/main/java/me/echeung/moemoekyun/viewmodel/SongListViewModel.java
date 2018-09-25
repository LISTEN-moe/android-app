package me.echeung.moemoekyun.viewmodel;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

public class SongListViewModel extends BaseObservable {

    private String query;
    private boolean hasResults;

    @Bindable
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
        notifyPropertyChanged(BR.query);
    }

    public void clearQuery() {
        setQuery(null);
    }

    @Bindable
    public boolean getHasResults() {
        return hasResults;
    }

    public void setHasResults(boolean hasResults) {
        this.hasResults = hasResults;
        notifyPropertyChanged(BR.hasResults);
    }

}
