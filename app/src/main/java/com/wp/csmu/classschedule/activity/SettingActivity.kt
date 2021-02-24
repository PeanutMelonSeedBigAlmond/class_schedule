package com.wp.csmu.classschedule.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import com.google.android.material.snackbar.Snackbar
import com.wp.csmu.classschedule.R
import com.wp.csmu.classschedule.fragment.ScheduleSettingFragment
import com.wp.csmu.classschedule.fragment.SettingFragment
import com.wp.csmu.classschedule.io.IO
import com.wp.csmu.classschedule.utils.ViewUtils
import kotlinx.android.synthetic.main.activity_setting.*
import java.io.File
import kotlin.system.exitProcess

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
            onResultOk(requestCode, data)
        }
    }

    private fun onResultOk(requestCode: Int, data: Intent?) {
        val fragment = supportFragmentManager.findFragmentByTag("ScheduleSettingFragment") as ScheduleSettingFragment
        when (requestCode) {
            //选择图片成功，裁剪图片
            fragment.SELECT_CODE -> cropBackground(data!!.data!!)
            //裁剪图片成功，启动处理图片
            fragment.CROP_CODE -> startActivityForResult(Intent(this, ImageProcessActivity::class.java), fragment.SUCCESS_CODE)
            //处理成功
            fragment.SUCCESS_CODE -> showRestartTip()
        }
    }

    private fun cropBackground(uri: Uri) {
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(uri, "image/*")
        intent.putExtra("crop", "true")

        //设置宽高比
        val pixels = ViewUtils.getScreenPixels(this)
        intent.putExtra("aspectX", pixels[0])
        intent.putExtra("aspectY", pixels[1])

        intent.putExtra("output", Uri.fromFile(File(IO.backgroundImg)))
        intent.putExtra("outputFormat", "JPEG")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        startActivityForResult(intent, 1)
    }

    fun showRestartTip() {
        Snackbar.make(settingCoordinatorLayout!!, "重启生效", Snackbar.LENGTH_INDEFINITE).setAction("重启") {
            val intent = baseContext.packageManager
                    .getLaunchIntentForPackage(baseContext.packageName)
            startActivity(intent)
            exitProcess(0)
        }.show()
    }
}
