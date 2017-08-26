package me.echeung.moemoekyun.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.ui.fragments.RadioFragment;
import me.echeung.moemoekyun.ui.fragments.UserFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private final SparseArray<WeakReference<Fragment>> mFragmentArray = new SparseArray<>();
    private final List<Holder> mHolderList = new ArrayList<>();

    @NonNull
    private final Context mContext;

    private final int[] icons;

    public ViewPagerAdapter(@NonNull final Context context, final FragmentManager fragmentManager) {
        super(fragmentManager);
        mContext = context;

        icons = new int[]{
                R.drawable.ic_radio_white_24dp,
                R.drawable.ic_person_white_24dp
        };

        // TODO: clean up
        final AppFragments[] fragments = AppFragments.values();
        for (final AppFragments fragment : fragments) {
            add(fragment.getFragmentClass(), null);
        }
    }

    @SuppressWarnings("synthetic-access")
    public void add(@NonNull final Class<? extends Fragment> className, final Bundle params) {
        final Holder mHolder = new Holder();
        mHolder.mClassName = className.getName();
        mHolder.mParams = params;

        final int mPosition = mHolderList.size();
        mHolderList.add(mPosition, mHolder);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
        final Fragment mFragment = (Fragment) super.instantiateItem(container, position);
        final WeakReference<Fragment> mWeakFragment = mFragmentArray.get(position);
        if (mWeakFragment != null) {
            mWeakFragment.clear();
        }
        mFragmentArray.put(position, new WeakReference<>(mFragment));
        return mFragment;
    }

    @Override
    public Fragment getItem(final int position) {
        final Holder mCurrentHolder = mHolderList.get(position);
        return Fragment.instantiate(mContext,
                mCurrentHolder.mClassName, mCurrentHolder.mParams);
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object object) {
        super.destroyItem(container, position, object);
        final WeakReference<Fragment> mWeakFragment = mFragmentArray.get(position);
        if (mWeakFragment != null) {
            mWeakFragment.clear();
        }
    }

    @Override
    public int getCount() {
        return mHolderList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

    public int getIcon(int position) {
        return icons[position];
    }

    public enum AppFragments {
        SONG(RadioFragment.class),
        ALBUM(UserFragment.class);

        private final Class<? extends Fragment> mFragmentClass;

        AppFragments(final Class<? extends Fragment> fragmentClass) {
            mFragmentClass = fragmentClass;
        }

        public Class<? extends Fragment> getFragmentClass() {
            return mFragmentClass;
        }
    }

    private final static class Holder {
        String mClassName;
        Bundle mParams;
    }
}
