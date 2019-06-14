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
import com.wp.csmu.classschedule.config.TimetableViewConfig
import com.wp.csmu.classschedule.utils.DateUtils
import com.wp.csmu.classschedule.view.scheduletable.AppSubjects
import com.zhuangfei.timetable.TimetableView
import com.zhuangfei.timetable.listener.OnItemClickAdapter
import com.zhuangfei.timetable.model.Schedule

class ScheduleFragment : Fragment() {
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
        val week = arguments!!.getInt("week")
        Log.i("Tag", week.toString())
        timetableView = view.findViewById(R.id.scheduleFragmentTimeTableView)
        timetableView.source(AppSubjects.subjects.toMutableList())
        timetableView.isShowWeekends(TimetableViewConfig.isShowWeekday)
        timetableView.changeWeekForce(week)
        timetableView.showView()
        timetableView.onDateBuildListener().onUpdateDate(DateUtils.getCurrentWeek(TimetableViewConfig.termBeginsTime), week)
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
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(name)
        val view = LayoutInflater.from(context!!).inflate(R.layout.schedule_info_dialog, null)
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
