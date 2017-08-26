package me.echeung.moemoekyun.ui.fragments.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class TabFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    protected void runOnUiThread(Runnable runnable) {
        final Activity activity = getActivity();
        if (activity == null) return;

        activity.runOnUiThread(runnable);
    }
}
