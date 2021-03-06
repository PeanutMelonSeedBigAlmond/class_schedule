package com.wp.csmu.classschedule.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.wp.csmu.classschedule.R
import com.wp.csmu.classschedule.application.MyApplicationLike
import com.wp.csmu.classschedule.utils.DateUtils
import com.wp.csmu.classschedule.view.scheduletable.AppSubjects
import com.zhuangfei.timetable.TimetableView
import com.zhuangfei.timetable.listener.OnItemClickAdapter
import com.zhuangfei.timetable.model.Schedule

class ScheduleFragment private constructor() : Fragment() {
    companion object {
        fun newInstance(index: Int): ScheduleFragment {
            val fragment = ScheduleFragment()
            val bundle = Bundle()
            bundle.putInt("week", index)
            fragment.arguments = bundle
            return fragment
        }
    }

    private var mView: View? = null
    private lateinit var timetableView: TimetableView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_schedule, container, false)
        }
        return mView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val week = requireArguments().getInt("week")
        Log.i("Tag", week.toString())
        timetableView = view.findViewById(R.id.scheduleFragmentTimeTableView)
        timetableView.alpha(0f, 0f, 1f)
        timetableView.source(AppSubjects.subjects.toMutableList())
        timetableView.curWeek(DateUtils.getCurrentWeek(MyApplicationLike.configData.termBeginsTime))
        timetableView.isShowWeekends(MyApplicationLike.configData.isShowWeekday)
        timetableView.maxSlideItem(MyApplicationLike.configData.classesOfDay)
        timetableView.changeWeek(week, true)
        timetableView.showView()
        var tempWeek = 0
        timetableView.onDateBuildListener().onUpdateDate(
                if (DateUtils.getCurrentWeek(MyApplicationLike.configData.termBeginsTime).also {
                            tempWeek = it
                            /*Log.i("Schedule",it.toString())*/
                        } < 0)
                    tempWeek - 1
                else
                    tempWeek,
                week
        )
        timetableView.callback(object : OnItemClickAdapter() {
            override fun onItemClick(v: View?, scheduleList: List<Schedule>?) {
                for (schedule in scheduleList!!) {
                    if (schedule.weekList.contains(week)) {
                        Log.i("课程", schedule.name + "\t" + schedule.teacher)
                        showScheduleInfo(schedule)
                        break
                    }
                }
            }
        })
    }

    private fun showScheduleInfo(schedule: Schedule) {
        val name = schedule.name
        val room = schedule.room
        val teacher = schedule.teacher
        val weekList = schedule.weekList
        val stringBuilder = StringBuilder()
        for (i in weekList.indices) {
            stringBuilder.append(weekList[i])
            if (i != weekList.size - 1) {
                stringBuilder.append(", ")
            } else {
                stringBuilder.append(" 周")
            }
        }
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(name)
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.schedule_info_dialog, null)
        val t1 = view!!.findViewById<TextView>(R.id.scheduleInfoTextView1)
        val t2 = view.findViewById<TextView>(R.id.scheduleInfoTextView2)
        val t3 = view.findViewById<TextView>(R.id.scheduleInfoTextView3)
        t1.text = stringBuilder
        t2.text = teacher
        t3.text = room
        builder.setView(view)
        builder.create().show()
    }
}
