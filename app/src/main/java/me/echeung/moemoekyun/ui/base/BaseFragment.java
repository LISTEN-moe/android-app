package me.echeung.moemoekyun.ui.base;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;

public abstract class BaseFragment<T extends ViewDataBinding> extends Fragment {

    protected T binding;

    // Receiver
    private IntentFilter intentFilter;
    private BroadcastReceiver intentReceiver;
    private boolean receiverRegistered = false;

    @LayoutRes
    protected abstract int getLayout();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false);

        intentReceiver = getBroadcastReceiver();
        intentFilter = getIntentFilter();

        registerReceiver();

        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver();

        if (binding != null) {
            binding.unbind();
        }

        super.onDestroy();
    }

    protected BroadcastReceiver getBroadcastReceiver() {
        return null;
    }

    protected IntentFilter getIntentFilter() {
        return null;
    }

    private void registerReceiver() {
        if (!receiverRegistered && intentReceiver != null && intentFilter != null) {
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
