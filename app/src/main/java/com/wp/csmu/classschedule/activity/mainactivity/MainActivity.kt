package com.wp.csmu.classschedule.activity.mainactivity

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.snackbar.Snackbar
import com.wp.csmu.classschedule.R
import com.wp.csmu.classschedule.activity.BaseActivity
import com.wp.csmu.classschedule.activity.ScoreActivity
import com.wp.csmu.classschedule.activity.SettingActivity
import com.wp.csmu.classschedule.application.MyApplication
import com.wp.csmu.classschedule.fragment.ScheduleFragment
import com.wp.csmu.classschedule.io.IO
import com.wp.csmu.classschedule.network.service.ServiceClient
import com.wp.csmu.classschedule.utils.DateUtils
import com.wp.csmu.classschedule.view.scheduletable.AppSubjects
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.nashlegend.anypref.AnyPref
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import kotlin.system.exitProcess


class MainActivity : BaseActivity() {
    var lastToolbarClickTime: Long = -1500
    private lateinit var adapter: PagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initActionBar()
        loadData()
        initViewPager()
        initDrawerLayout()
        setBackground()
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
        this.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
            override fun getCount(): Int = MyApplication.configData.weeksOfTerm

            override fun getItem(position: Int): Fragment = ScheduleFragment.newInstance(position + 1)
        }
        mainViewPager.addOnPageChangeListener(MyOnPageChangeListener())
        mainViewPager.adapter = adapter
        gotoCurrentWeek()
    }

    private fun loadData() {
        AppSubjects.subjects = IO.readSchedule()
    }

    private fun gotoCurrentWeek() {
        mainViewPager.currentItem = DateUtils.getCurrentWeek(MyApplication.configData.termBeginsTime) - 1
    }

    private fun initDrawerLayout() {
        val toggle = ActionBarDrawerToggle(this, mainDrawerLayout, mainToolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        mainDrawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        mainNavigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.mainDrawerScore -> startActivity(Intent(this@MainActivity, ScoreActivity::class.java))
                R.id.mainDrawerRefresh -> {
                    AlertDialog.Builder(this@MainActivity).setMessage("刷新课程？")
                            .setPositiveButton("确定") { _, _ -> refreshSchedule() }
                            .setNegativeButton("取消") { _, _ -> }
                            .create().show()

                }
                R.id.mainDrawerSetting -> startActivity(Intent(this@MainActivity, SettingActivity::class.java))
                R.id.mainDrawerAbout -> try {
                    val inputStream = assets.open("about.html");
                    val text = IO.readString(inputStream);
                    val view = LayoutInflater.from(this@MainActivity).inflate(R.layout.about_dialog_layout, null, false);
                    val textView: TextView = view.findViewById(R.id.aboutDialogTextView);
                    val builder = AlertDialog.Builder(this@MainActivity);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        //api version > 24 (android n)
                        textView.text = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT);
                    } else {
                        textView.text = Html.fromHtml(text);
                    }
                    textView.movementMethod = LinkMovementMethod.getInstance();
                    builder.setTitle("关于").setView(view).show();
                } catch (e: IOException) {
                    e.printStackTrace()
                    Snackbar.make(loginCoordinatorLayout!!, e.toString(), Snackbar.LENGTH_SHORT).show()
                }
            }
            mainDrawerLayout.closeDrawers()
            return@setNavigationItemSelectedListener true
        }
    }

    private fun refreshSchedule() {
        GlobalScope.launch(Dispatchers.Main) {
            mainSwipeRefreshLayout.isRefreshing = true
            try {
                val terms = withContext(Dispatchers.IO) { ServiceClient.getTermId() }
                showTermSelection(terms)
            } catch (e: Exception) {
                e.printStackTrace()
                onOperationFailed(e)
            }
        }
    }

    private fun showTermSelection(terms: HashMap<Pair<String, String>, Boolean>) {
        var selectedTermIndex = terms.findValue(true)
        val termId = terms.keys.map { it.first }
        val termName = terms.keys.map { it.second }
        val dialog = AlertDialog.Builder(this)
                .setTitle("选择学期")
                .setSingleChoiceItems(
                        termName.toTypedArray(),
                        selectedTermIndex
                ) { _, i -> selectedTermIndex = i }
                .setPositiveButton(
                        "确定"
                ) { _, _ -> onOperationSucceed(termId[selectedTermIndex]) }
                .setNegativeButton("取消") { _, _ -> mainSwipeRefreshLayout.isRefreshing = false }
        dialog.create().show()
    }

    private fun onOperationSucceed(selectedTermId: String) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val schedule = withContext(Dispatchers.IO) { ServiceClient.getScheduleList(selectedTermId) }
                //写入课程
                IO.writeSchedule(schedule)

                val termBeginsTime = withContext(Dispatchers.IO) { ServiceClient.getTermBeginsTime(selectedTermId) }
                val termId = MyApplication.configData.currentTermId
                val config = MyApplication.configData.also {
                    it.termBeginsTime = termBeginsTime
                    it.currentTermId = selectedTermId
                }
                AnyPref.put(config)
                mainSwipeRefreshLayout.isRefreshing = false

                if (termId != selectedTermId) {
                    Snackbar.make(mainCoordinatorLayout!!, "学期已经更新，重启生效", Snackbar.LENGTH_INDEFINITE).setAction("重启") {
                        val intent = baseContext.packageManager
                                .getLaunchIntentForPackage(baseContext.packageName)
                        startActivity(intent)
                        exitProcess(0)
                    }.show()
                }else{
                    Snackbar.make(mainCoordinatorLayout!!, "刷新成功", Snackbar.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                onOperationFailed(e)
                mainSwipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun onOperationFailed(reason: Exception) {
        Snackbar.make(mainCoordinatorLayout!!, reason.toString(), Snackbar.LENGTH_SHORT).show()
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

                tv1.text = (mainViewPager.currentItem + 1).toString()
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

    private fun setBackground() {
        val background = File(IO.backgroundImg)
        if (background.exists()) {
            try {
                val fis = FileInputStream(background)
                val bitmap: Bitmap = BitmapFactory.decodeStream(fis)
                mainConstraintLayout.background = BitmapDrawable(resources, bitmap)
                fis.close()
            } catch (e: Exception) {
            }
        }
    }

    // 寻找map中第一次出现value的index
    private fun <K, V> Map<K, V>.findValue(value: V): Int {
        var index = 0
        this.entries.forEachIndexed { i, entry ->
            if (entry.value == value) {
                index = i
                return@forEachIndexed
            }
        }
        return index
    }
}