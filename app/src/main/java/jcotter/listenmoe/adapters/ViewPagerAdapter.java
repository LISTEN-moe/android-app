package jcotter.listenmoe.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import jcotter.listenmoe.ui.fragments.RadioFragment;
import jcotter.listenmoe.ui.fragments.UserFragment;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    private static RadioFragment radioFragment;
    private static UserFragment userFragment;

    public ViewPagerAdapter(FragmentManager fm, Context mContext) {
        super(fm);
    }

    public static RadioFragment getRadioFragment() {
        return radioFragment;
    }

    public static UserFragment getUserFragment() {
        return userFragment;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return RadioFragment.newInstance(1);
            case 1:
                return UserFragment.newInstance(2);
            default:
                return null;
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final Fragment createdFragment = (Fragment) super.instantiateItem(container, position);

        // Save fragment references depending on position
        switch (position) {
            case 0:
                radioFragment = (RadioFragment) createdFragment;
                break;
            case 1:
                userFragment = (UserFragment) createdFragment;
                break;
            case 2:
        }

        return createdFragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // TODO
        if (position == 0) {
            return "Radio";
        } else {
            return "User";
        }
    }
}
