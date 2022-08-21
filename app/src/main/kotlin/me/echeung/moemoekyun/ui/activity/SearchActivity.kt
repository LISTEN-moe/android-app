package me.echeung.moemoekyun.ui.activity

import android.os.Bundle
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.base.BaseActivity

class SearchActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initAppbar()
    }
}
