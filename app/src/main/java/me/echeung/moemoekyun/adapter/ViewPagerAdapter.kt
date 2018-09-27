package me.echeung.moemoekyun.adapter

import android.content.Context
import android.os.Bundle
import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.fragment.SongsFragment
import me.echeung.moemoekyun.ui.fragment.UserFragment
import java.lang.ref.WeakReference
import java.util.*

class ViewPagerAdapter(private val context: Context, fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    private val fragments = SparseArray<WeakReference<Fragment>>()
    private val holders = ArrayList<TabHolder>()

    init {
        // Tabs
        add(UserFragment::class.java, R.drawable.ic_person_white_24dp)
        add(SongsFragment::class.java, R.drawable.ic_audiotrack_white_24dp)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as Fragment
        val fragmentRef = fragments.get(position)
        fragmentRef?.clear()
        fragments.put(position, WeakReference(fragment))
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
        val mWeakFragment = fragments.get(position)
        mWeakFragment?.clear()
    }

    override fun getCount(): Int {
        return holders.size
    }

    override fun getItem(position: Int): Fragment {
        val holder = holders[position]
        return Fragment.instantiate(context, holder.className, holder.params)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return ""
    }

    fun getIcon(position: Int): Int {
        return holders[position].drawableId
    }

    private fun add(className: Class<out Fragment>, drawableId: Int) {
        val holder = TabHolder()
        holder.className = className.name
        holder.drawableId = drawableId
        holder.params = null

        holders.add(holder)
        notifyDataSetChanged()
    }

    private class TabHolder {
        internal var className: String? = null
        internal var drawableId: Int = 0
        internal var params: Bundle? = null
    }

}
