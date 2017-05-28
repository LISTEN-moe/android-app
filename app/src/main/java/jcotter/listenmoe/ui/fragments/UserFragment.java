package jcotter.listenmoe.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jcotter.listenmoe.R;
import jcotter.listenmoe.ui.fragments.base.TabFragment;

public class UserFragment extends TabFragment {
    public static Fragment newInstance(int sectionNumber) {
        return TabFragment.newInstance(sectionNumber, new UserFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_user, container, false);

        return rootView;
    }
}
