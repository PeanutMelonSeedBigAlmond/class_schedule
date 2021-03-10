package com.wp.csmu.classschedule.fragment


import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.DatePicker
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.wp.csmu.classschedule.R
import com.wp.csmu.classschedule.activity.SettingActivity
import com.wp.csmu.classschedule.application.MyApplicationLike
import com.wp.csmu.classschedule.io.IO
import net.nashlegend.anypref.AnyPref
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ScheduleSettingFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {
    val CROP_CODE = 1
    val SELECT_CODE = 3
    val SUCCESS_CODE = 2
    lateinit var weeksOfTerm: Preference
    lateinit var classesOfDay: Preference
    lateinit var termBeginTime: Preference
    lateinit var setBackground: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.schedule_setting_fragment_prefences)
        weeksOfTerm = findPreference("weeks_of_term")!!
        classesOfDay = findPreference("classes_of_day")!!
        termBeginTime = findPreference("term_begins_time")!!
        setBackground = findPreference("set_background")!!

        weeksOfTerm.onPreferenceClickListener = this
        classesOfDay.onPreferenceClickListener = this
        termBeginTime.onPreferenceClickListener = this
        setBackground.onPreferenceClickListener = this
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        when (preference!!) {
            weeksOfTerm -> {
                showWeeksSelect()
                return true
            }
            classesOfDay -> {
                showClassesSelect()
                return true
            }
            termBeginTime -> {
                showBeginTimeSelect()
                return true
            }
            setBackground -> {
                showDialog()
                return true
            }
        }
        return false
    }

    private fun showDialog() {
        val file = File(IO.backgroundImg)
        if (file.exists()) {
            val dialog = AlertDialog.Builder(activity)
            dialog.setMessage("选择")
                    .setPositiveButton("选择新图片") { _, _ -> selectBackground() }
                    .setNeutralButton("清除背景图片") { _, _ ->
                        run {
                            file.delete()
                            (requireActivity() as SettingActivity).showRestartTip()
                        }
                    }
                    .show()
        } else {
            selectBackground()
        }
    }

    private fun selectBackground() {
        //选择并裁剪图片
//        val intent = Intent(Intent.ACTION_GET_CONTENT)
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        requireActivity().startActivityForResult(intent, SELECT_CODE)
    }

    private fun showWeeksSelect() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.weeks_classes_selector, null)
        val tv1 = view.findViewById<TextView>(R.id.weeksClassesSelectorTextView1)
        val tv2 = view.findViewById<TextView>(R.id.weeksClassesSelectorTextView2)
        val tv3 = view.findViewById<TextView>(R.id.weeksClassesSelectorTextView3)
        val seekBar = view.findViewById<SeekBar>(R.id.weeksClassesSelectorSeekBar)
        seekBar.max = 50
        seekBar.progress = MyApplicationLike.configData.weeksOfTerm
        tv1.text = seekBar.progress.toString()
        tv2.text = "0"
        tv3.text = seekBar.max.toString()
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tv1.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
        builder.setTitle("学期周数").setView(view).setPositiveButton("确定") { _, _ ->
            MyApplicationLike.configData.weeksOfTerm = seekBar.progress
            AnyPref.put(MyApplicationLike.configData)
            (requireActivity() as SettingActivity).showRestartTip()
        }
                .setNegativeButton("取消") { _, _ -> }
                .setNeutralButton("恢复默认") { _, _ ->
                    MyApplicationLike.configData.weeksOfTerm = 20
                    AnyPref.put(MyApplicationLike.configData)
                    (requireActivity() as SettingActivity).showRestartTip()
                }
                .show()
    }

    private fun showClassesSelect() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.weeks_classes_selector, null)
        val tv1 = view.findViewById<TextView>(R.id.weeksClassesSelectorTextView1)
        val tv2 = view.findViewById<TextView>(R.id.weeksClassesSelectorTextView2)
        val tv3 = view.findViewById<TextView>(R.id.weeksClassesSelectorTextView3)
        val seekBar = view.findViewById<SeekBar>(R.id.weeksClassesSelectorSeekBar)
        seekBar.max = 20
        seekBar.progress = MyApplicationLike.configData.classesOfDay
        tv1.text = seekBar.progress.toString()
        tv2.text = "0"
        tv3.text = seekBar.max.toString()
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tv1.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
        builder.setTitle("一天课程数").setView(view).setPositiveButton("确定") { _, _ ->
            MyApplicationLike.configData.classesOfDay = seekBar.progress
            AnyPref.put(MyApplicationLike.configData)
            (requireActivity() as SettingActivity).showRestartTip()
        }
                .setNegativeButton("取消") { _, _ -> }
                .setNeutralButton("恢复默认") { _, _ ->
                    MyApplicationLike.configData.classesOfDay = seekBar.progress
                    AnyPref.put(MyApplicationLike.configData)
                    (requireActivity() as SettingActivity).showRestartTip()
                }
                .show()
    }

    private fun showBeginTimeSelect() {
        Toast.makeText(context, "为了周次计算的准确，建议选择周一", Toast.LENGTH_SHORT).show()
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.term_begins_time_selector, null)
        val datePicker = view.findViewById<DatePicker>(R.id.termBeginsTimeSelectorDatePicker)
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val time = MyApplicationLike.configData.termBeginsTime
        val date = Calendar.getInstance()
        date.time = sdf.parse(time)
        datePicker.init(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)) { _, _, _, _ -> }

        builder.setTitle("开学日期").setView(view).setPositiveButton("确定") { _, _ ->
            MyApplicationLike.configData.termBeginsTime = "${datePicker.year}-${datePicker.month + 1}-${datePicker.dayOfMonth}"
            AnyPref.put(MyApplicationLike.configData)
            (requireActivity() as SettingActivity).showRestartTip()
        }
                .setNegativeButton("取消") { _, _ -> }
                .setNeutralButton("恢复默认") { _, _ ->
                    run {
                        MyApplicationLike.configData.termBeginsTime = ""
                        AnyPref.put(MyApplicationLike.configData)
                        (requireActivity() as SettingActivity).showRestartTip()
                    }
                }
                .show()
    }
}
