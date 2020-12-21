package com.wp.csmu.classschedule.activity.mainactivity

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.wp.csmu.classschedule.R
import com.wp.csmu.classschedule.activity.BaseActivity
import com.wp.csmu.classschedule.application.MyApplication
import com.wp.csmu.classschedule.fragment.ScheduleFragment
import com.wp.csmu.classschedule.io.IO
import com.wp.csmu.classschedule.utils.DateUtils
import com.wp.csmu.classschedule.view.scheduletable.AppSubjects
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    var lastToolbarClickTime: Long = -1500
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initActionBar()
        loadData()
        initViewPager()
    }

    private fun initActionBar() {
        setSupportActionBar(mainToolBar)
        mainToolBar.setOnClickListener {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastToolbarClickTime <= 500) {
                gotoCurrentWeek()
            }

            lastToolbarClickTime = currentTime
        }
    }

    private fun initViewPager() {
        mainViewPager.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
            override fun getCount(): Int = MyApplication.configData.weeksOfTerm

            override fun getItem(position: Int): Fragment = ScheduleFragment.newInstance(position + 1)
        }
        mainViewPager.addOnPageChangeListener(MyOnPageChangeListener())

        gotoCurrentWeek()
    }

    private fun loadData() {
        AppSubjects.subjects = IO.readSchedule()
    }

    private fun gotoCurrentWeek() {
        mainViewPager.currentItem = DateUtils.getCurrentWeek(MyApplication.configData.termBeginsTime) - 1
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.gotoWeek -> {
                val view = LayoutInflater.from(this).inflate(R.layout.weeks_classes_selector, null, false)
                val tv1: TextView = view.findViewById(R.id.weeksClassesSelectorTextView1)
                val tv2: TextView = view.findViewById(R.id.weeksClassesSelectorTextView2)
                val tv3: TextView = view.findViewById(R.id.weeksClassesSelectorTextView3)
                val sb: SeekBar = view.findViewById(R.id.weeksClassesSelectorSeekBar)

                tv1.text = (mainViewPager.currentItem+1).toString()
                sb.progress = mainViewPager.currentItem

                tv2.text = "1"
                tv3.text = MyApplication.configData.weeksOfTerm.toString()
                sb.max = MyApplication.configData.weeksOfTerm - 1
                sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        tv1.text = (progress + 1).toString()
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })

                val dialog = AlertDialog.Builder(this)
                        .setView(view)
                        .setTitle("跳转")
                        .setPositiveButton("确定") { _, _ -> mainViewPager.currentItem = sb.progress }
                        .setNegativeButton("取消") { _, _ -> }
                dialog.create().show()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private inner class MyOnPageChangeListener : OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        override fun onPageSelected(position: Int) {
            supportActionBar!!.subtitle = "第" + (position + 1) + "周"
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }
}