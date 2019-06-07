package com.wp.csmu.classschedule.activity

import android.os.Bundle
import android.os.PersistableBundle
import com.wp.csmu.classschedule.R
import com.wp.csmu.classschedule.fragment.SettingFragment
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        setSupportActionBar(settingToolBar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.beginTransaction()
                .add(settingFrameLayout.id, SettingFragment(), "SettingFragment")
                .commit()
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        //super.onSaveInstanceState(outState, outPersistentState)
    }
}
