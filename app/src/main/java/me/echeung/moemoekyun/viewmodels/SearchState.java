package me.echeung.moemoekyun.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

public class SearchState extends BaseObservable {

    private static final SearchState INSTANCE = new SearchState();

    public final ObservableBoolean hasResults = new ObservableBoolean(false);
    public final ObservableField<String> query = new ObservableField<>();

    private SearchState() {}

    public static SearchState getInstance() {
        return INSTANCE;
    }

    public void reset() {
        hasResults.set(false);
        query.set(null);
    }
}
