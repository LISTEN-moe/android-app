package me.echeung.moemoekyun.ui.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class BaseDataBindingActivity<T : ViewDataBinding> : BaseActivity() {

    protected lateinit var binding: T

    @LayoutRes
    protected var layout: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, layout)
    }

    override fun onDestroy() {
        binding.unbind()

        super.onDestroy()
    }
}
