package com.wp.csmu.classschedule.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import com.google.android.material.snackbar.Snackbar
import com.wp.csmu.classschedule.R
import com.wp.csmu.classschedule.fragment.ScheduleSettingFragment
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

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            onResultOk(requestCode)
        } else {
        }
    }

    private fun onResultOk(requestCode: Int) {
        val fragment = supportFragmentManager.findFragmentByTag("ScheduleSettingFragment") as ScheduleSettingFragment
        if (requestCode == fragment.CROP_CODE) {
            startActivityForResult(Intent(this, ImageProcessActivity::class.java), 2)
        } else if (requestCode == 2) {
            showTextWithSnackBar("设置成功，重启生效")
        }
    }

    public fun showTextWithSnackBar(text: String) {
        Snackbar.make(settingCoordinatorLayout, text, Snackbar.LENGTH_SHORT).show()
    }
}
