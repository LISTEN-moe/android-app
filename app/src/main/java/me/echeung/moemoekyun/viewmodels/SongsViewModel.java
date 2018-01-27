package me.echeung.moemoekyun.viewmodels;

import android.content.Context;
import android.databinding.Bindable;

import com.android.databinding.library.baseAdapters.BR;

public class SongsViewModel extends BaseViewModel {

    private boolean hasResults;

    public SongsViewModel(Context context) {
        super(context);

        // Defaults
        hasResults = false;
    }

    public void reset() {
        setHasResults(false);
    }

    @Bindable
    public boolean getHasResults() {
        return hasResults;
    }

    public void setHasResults(boolean hasResults) {
        this.hasResults = hasResults;
        notifyPropertyChanged(BR.hasResults);
    }

//    @Bindable
//    public String getQuery() {
//        final Context context = getContext();
//        if (context == null || TextUtils.isEmpty(query)) {
//            return null;
//        }
//
//        return String.format(context.getString(R.string.no_results), query);
//    }

}
