package jcotter.listenmoe.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import jcotter.listenmoe.R;
import jcotter.listenmoe.ui.fragments.RadioFragment;
import jcotter.listenmoe.ui.fragments.UserFragment;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    final private int[] ICONS = new int[]{
            R.drawable.ic_radio_white_24dp,
            R.drawable.ic_person_white_24dp
    };

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
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
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

    /**
     * Gets the tab's icon.
     *
     * @param position The tab index.
     * @return The resource ID for the tab's icon.
     */
    public int getIcon(int position) {
        return ICONS[position];
    }
}
