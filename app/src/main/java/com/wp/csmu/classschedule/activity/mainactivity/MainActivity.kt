package com.wp.csmu.classschedule.activity.mainactivity

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.wp.csmu.classschedule.R
import com.wp.csmu.classschedule.activity.BaseActivity
import com.wp.csmu.classschedule.data.sharedpreferences.TimetableViewConfigData
import com.wp.csmu.classschedule.fragment.ScheduleFragment
import com.wp.csmu.classschedule.io.IO
import com.wp.csmu.classschedule.view.scheduletable.AppSubjects
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.weeks_classes_selector.*
import net.nashlegend.anypref.AnyPref

class MainActivity : BaseActivity() {
    var currentWeek = 0
    var lastToolbarClickTime: Long = -1500
    private lateinit var configData: TimetableViewConfigData
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initActionBar()
        initConfig()
        loadData()
        initViewPager()
    }

    private fun initActionBar() {
        setSupportActionBar(mainToolBar)
        mainToolBar.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastToolbarClickTime > 500) {
                lastToolbarClickTime = currentTime
            } else {
                mainViewPager.currentItem = currentWeek - 1
                lastToolbarClickTime = currentTime
            }
        }
    }

    private fun initConfig() {
        configData = AnyPref.get(TimetableViewConfigData::class.java)
    }

    private fun initViewPager() {
        mainViewPager.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
            override fun getCount(): Int = configData.weeksOfTerm

            override fun getItem(position: Int): Fragment = ScheduleFragment.newInstance(position + 1)
        }
        mainViewPager.addOnPageChangeListener(MyOnPageChangeListener())
    }

    private fun loadData() {
        AppSubjects.subjects = IO.readSchedule()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.gotoWeek -> {
                val config = AnyPref.get(TimetableViewConfigData::class.java)
                val weeksOfTerm = config.weeksOfTerm
                val view = LayoutInflater.from(this).inflate(R.layout.weeks_classes_selector, null, false)
                weeksClassesSelectorTextView2.text = "1"
                weeksClassesSelectorTextView3.text = weeksOfTerm.toString()
                weeksClassesSelectorSeekBar.max = weeksOfTerm - 1
                weeksClassesSelectorSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        weeksClassesSelectorTextView1.text = (progress + 1).toString()
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })

                val dialog = AlertDialog.Builder(this)
                        .setView(view)
                        .setPositiveButton("确定") { _, _ -> mainViewPager.currentItem = weeksClassesSelectorSeekBar.progress }
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