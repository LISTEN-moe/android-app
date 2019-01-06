package me.echeung.moemoekyun.ui.base

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class BaseFragment<T : ViewDataBinding> : Fragment() {

    protected lateinit var binding: T

    // Receiver
    private var intentFilter: IntentFilter? = null
    private var intentReceiver: BroadcastReceiver? = null
    private var receiverRegistered = false

    @LayoutRes
    protected var layout: Int = 0

    protected var broadcastReceiver: BroadcastReceiver? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, layout, container, false)

        intentReceiver = broadcastReceiver
        intentFilter = getIntentFilter()

        registerReceiver()

        return binding.root
    }

    override fun onDestroy() {
        unregisterReceiver()

        binding?.unbind()

        super.onDestroy()
    }

    protected open fun getIntentFilter(): IntentFilter? {
        return null
    }

    private fun registerReceiver() {
        if (!receiverRegistered && intentReceiver != null && intentFilter != null) {
            requireActivity().registerReceiver(intentReceiver, intentFilter)
            receiverRegistered = true
        }
    }

    private fun unregisterReceiver() {
        if (receiverRegistered) {
            requireActivity().unregisterReceiver(intentReceiver)
            receiverRegistered = false
        }
    }
}
