package me.echeung.moemoekyun.ui.base;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseFragment<T extends ViewDataBinding> extends Fragment {

    protected T binding;

    // Receiver
    private IntentFilter intentFilter;
    private BroadcastReceiver intentReceiver;
    private boolean receiverRegistered = false;

    public abstract int getLayout();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false);

        intentReceiver = getBroadcastReceiver();
        intentFilter = getIntentFilter();

        registerReceiver();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!receiverRegistered) {
            registerReceiver();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver();

        if (binding != null) {
            binding.unbind();
        }

        super.onDestroy();
    }

    public abstract BroadcastReceiver getBroadcastReceiver();

    public abstract IntentFilter getIntentFilter();

    private void registerReceiver() {
        if (intentReceiver != null && intentFilter != null) {
            getActivity().registerReceiver(intentReceiver, intentFilter);
            receiverRegistered = true;
        }
    }

    private void unregisterReceiver() {
        if (receiverRegistered) {
            getActivity().unregisterReceiver(intentReceiver);
            receiverRegistered = false;
        }
    }

}
