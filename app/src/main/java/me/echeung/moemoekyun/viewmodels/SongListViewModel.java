package me.echeung.moemoekyun.viewmodels;

import android.content.Context;
import android.databinding.Bindable;
import android.text.TextUtils;

import com.android.databinding.library.baseAdapters.BR;

import me.echeung.moemoekyun.R;

public class SongListViewModel extends BaseViewModel {

    private String query;
    private boolean hasResults;

    public SongListViewModel(Context context) {
        super(context);
    }

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

    // TODO: this isn't showing up
    @Bindable
    public String getNoResults() {
        final Context context = getContext();
        if (context == null || TextUtils.isEmpty(query)) {
            return null;
        }

        return String.format(context.getString(R.string.no_results), query);
    }

    public void setNoResults(String query) {
        this.query = query;
        notifyPropertyChanged(BR.noResults);
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
