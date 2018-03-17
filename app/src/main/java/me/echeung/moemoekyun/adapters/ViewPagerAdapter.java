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
import me.echeung.moemoekyun.ui.fragments.SongsFragment;
import me.echeung.moemoekyun.ui.fragments.UserFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private final SparseArray<WeakReference<Fragment>> fragments = new SparseArray<>();
    private final List<TabHolder> holders = new ArrayList<>();

    @NonNull
    private final Context context;

    public ViewPagerAdapter(@NonNull final Context context, final FragmentManager fragmentManager) {
        super(fragmentManager);
        this.context = context;

        // Tabs
        add(UserFragment.class, R.drawable.ic_person_white_24dp);
        add(SongsFragment.class, R.drawable.ic_audiotrack_white_24dp);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
        final Fragment fragment = (Fragment) super.instantiateItem(container, position);
        final WeakReference<Fragment> fragmentRef = fragments.get(position);
        if (fragmentRef != null) {
            fragmentRef.clear();
        }
        fragments.put(position, new WeakReference<>(fragment));
        return fragment;
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object object) {
        super.destroyItem(container, position, object);
        final WeakReference<Fragment> mWeakFragment = fragments.get(position);
        if (mWeakFragment != null) {
            mWeakFragment.clear();
        }
    }

    @Override
    public int getCount() {
        return holders.size();
    }

    @Override
    public Fragment getItem(final int position) {
        final TabHolder holder = holders.get(position);
        return Fragment.instantiate(context, holder.className, holder.params);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

    public int getIcon(int position) {
        return holders.get(position).drawableId;
    }

    private void add(@NonNull final Class<? extends Fragment> className, final int drawableId) {
        final TabHolder holder = new TabHolder();
        holder.className = className.getName();
        holder.drawableId = drawableId;
        holder.params = null;

        holders.add(holder);
        notifyDataSetChanged();
    }

    private final static class TabHolder {
        String className;
        int drawableId;
        Bundle params;
    }

}
