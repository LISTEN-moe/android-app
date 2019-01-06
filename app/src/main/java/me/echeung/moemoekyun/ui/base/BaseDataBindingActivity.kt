package me.echeung.moemoekyun.ui.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import me.echeung.moemoekyun.R

abstract class BaseDataBindingActivity<T : ViewDataBinding> : BaseActivity() {

    protected lateinit var binding: T

    @LayoutRes
    protected var layout: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, layout)

        setSupportActionBar(findViewById(R.id.appbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    override fun onDestroy() {
        binding?.unbind()

        super.onDestroy()
    }
}
