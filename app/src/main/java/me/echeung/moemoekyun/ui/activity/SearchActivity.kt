package me.echeung.moemoekyun.ui.activity

import android.os.Bundle
import android.view.Menu
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.base.BaseActivity

class SearchActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initAppbar()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_sort, menu)

        return true
    }
}
