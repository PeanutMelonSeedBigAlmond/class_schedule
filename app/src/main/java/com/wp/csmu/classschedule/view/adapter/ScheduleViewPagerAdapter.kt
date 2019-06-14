package com.wp.csmu.classschedule.view.adapter

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.wp.csmu.classschedule.fragment.ScheduleFragment

class ScheduleViewPagerAdapter(fm: FragmentManager, var fragments: List<ScheduleFragment>) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): ScheduleFragment {
        return ScheduleFragment.newInstance(index = position + 1)
    }

    override fun getCount(): Int {
        return fragments.size
    }

    public fun fragmentChanged(fragments: List<ScheduleFragment>) {
        this.fragments = fragments
        notifyDataSetChanged()
    }
}