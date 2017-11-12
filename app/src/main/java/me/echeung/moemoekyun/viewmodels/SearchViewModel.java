package me.echeung.moemoekyun.viewmodels;

import android.content.Context;
import android.databinding.Bindable;
import android.text.TextUtils;

import com.android.databinding.library.baseAdapters.BR;

import me.echeung.moemoekyun.R;

public class SearchViewModel extends BaseViewModel {

    private boolean hasResults;
    private String query;
    private boolean showClearButton;

    public SearchViewModel(Context context) {
        super(context);

        // Defaults
        hasResults = false;
        showClearButton = false;
    }

    public void reset() {
        setHasResults(false);
        setShowClearButton(false);
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

    @Bindable
    public String getQuery() {
        final Context context = getContext();
        if (context == null || TextUtils.isEmpty(query)) {
            return null;
        }

        return String.format(context.getString(R.string.no_results), query);
    }

    public void setQuery(String query) {
        this.query = query;
        notifyPropertyChanged(BR.query);
    }

    @Bindable
    public boolean getShowClearButton() {
        return showClearButton;
    }

    public void setShowClearButton(boolean showClearButton) {
        this.showClearButton = showClearButton;
        notifyPropertyChanged(BR.showClearButton);
    }
}
