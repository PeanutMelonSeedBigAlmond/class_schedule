package com.wp.csmu.classschedule.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.tencent.bugly.beta.Beta
import com.wp.csmu.classschedule.R
import com.wp.csmu.classschedule.activity.BaseActivity
import com.wp.csmu.classschedule.activity.LoginActivity

class SettingFragment : Fragment() {
    lateinit var listView: ListView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_setting, container, false)
        with(root) {
            listView = findViewById(R.id.settingFragmentListView)
        }
        listView.adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, context!!.resources.getStringArray(R.array.fragmentSettingListViewArray))
        setListeners()
        return root

    }

    fun setListeners() {
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> {
                    //切换到另一个设置
                    fragmentManager!!.beginTransaction()
                            .replace(R.id.settingFrameLayout, ScheduleSettingFragment(), "ScheduleSettingFragment")
                            .addToBackStack("ScheduleSettingFragment")
                            .commit()
                }
                1 -> {
                    Beta.checkUpgrade()
                }
                2 -> {
                    logout()
                }
            }
        }
    }

    fun logout() {
        val dialog = AlertDialog.Builder(context).setMessage("确定退出登录吗").setPositiveButton("确定") { dialog, which ->
            val sharedPreferences = context!!.getSharedPreferences("user", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.commit()
            BaseActivity().finishAllActivity()
            startActivity(Intent(activity, LoginActivity::class.java))
        }.setNegativeButton("取消") { dialog, which -> }
        dialog.show()
    }
}
