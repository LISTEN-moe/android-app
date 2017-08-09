package me.echeung.moemoekyun.state;

import android.databinding.BaseObservable;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;

public class UserState extends BaseObservable {

    private static final UserState INSTANCE = new UserState();

    public final ObservableField<String> userName = new ObservableField<>();
    public final ObservableInt userRequests = new ObservableInt();
//        public final ObservableList<Song> favorites

    private UserState() {}

    public static UserState getInstance() {
        return INSTANCE;
    }

    public void clear() {
        userName.set(null);
        userRequests.set(0);
    }
}