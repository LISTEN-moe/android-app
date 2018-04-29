package me.echeung.moemoekyun.ui.base;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false);

        initBroadcastReceiver();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!receiverRegistered) {
            getActivity().registerReceiver(intentReceiver, intentFilter);
            receiverRegistered = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (receiverRegistered) {
            getActivity().unregisterReceiver(intentReceiver);
            receiverRegistered = false;
        }
    }

    @Override
    public void onDestroy() {
        if (receiverRegistered) {
            getActivity().unregisterReceiver(intentReceiver);
            receiverRegistered = false;
        }

        if (binding != null) {
            binding.unbind();
        }

        super.onDestroy();
    }

    public abstract BroadcastReceiver getBroadcastReceiver();

    public abstract IntentFilter getIntentFilter();

    private void initBroadcastReceiver() {
        intentReceiver = getBroadcastReceiver();
        intentFilter = getIntentFilter();

        getActivity().registerReceiver(intentReceiver, intentFilter);
        receiverRegistered = true;
    }

}
