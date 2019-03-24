package me.echeung.moemoekyun.adapter

import android.content.Context
import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.fragment.SongsFragment
import me.echeung.moemoekyun.ui.fragment.UserFragment
import java.lang.ref.WeakReference
import java.util.ArrayList

class ViewPagerAdapter(private val context: Context, fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    private val fragments = SparseArray<WeakReference<Fragment>>()
    private val holders = ArrayList<TabHolder>()

    init {
        // Tabs
        add(UserFragment::class.java, R.drawable.ic_person_24dp)
        add(SongsFragment::class.java, R.drawable.ic_audiotrack_24dp)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as Fragment
        fragments.get(position)?.clear()
        fragments.put(position, WeakReference(fragment))
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
        val fragment = fragments.get(position)
        fragment?.clear()
    }

    override fun getCount(): Int {
        return holders.size
    }

    override fun getItem(position: Int): Fragment {
        val holder = holders[position]
        return Fragment.instantiate(context, holder.className)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return ""
    }

    fun getIcon(position: Int): Int {
        return holders[position].drawableId
    }

    private fun add(className: Class<out Fragment>, drawableId: Int) {
        holders.add(TabHolder(className.name, drawableId))
        notifyDataSetChanged()
    }

    private data class TabHolder(
        val className: String,
        val drawableId: Int
    )
}
