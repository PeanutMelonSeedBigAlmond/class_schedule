package com.wp.csmu.classschedule.data.sharedpreferences;

import com.wp.csmu.classschedule.utils.DateUtils;

import net.nashlegend.anypref.annotations.PrefField;
import net.nashlegend.anypref.annotations.PrefModel;

@PrefModel("com.wp.csmu.classschedule_preferences")
public class TimetableViewConfigData {
    public String getTermBeginsTime() {
        return termBeginsTime;
    }

    public void setTermBeginsTime(String termBeginsTime) {
        this.termBeginsTime = termBeginsTime;
    }

    public boolean isShowWeekday() {
        return showWeekday;
    }

    public void setShowWeekday(boolean showWeekday) {
        this.showWeekday = showWeekday;
    }

    public int getClassesOfDay() {
        return classesOfDay;
    }

    public void setClassesOfDay(int classesOfDay) {
        this.classesOfDay = classesOfDay;
    }

    public int getWeeksOfTerm() {
        return weeksOfTerm;
    }

    public void setWeeksOfTerm(int weeksOfTerm) {
        this.weeksOfTerm = weeksOfTerm;
    }

    @PrefField(value = "term_begins_time")
    String termBeginsTime ;
    @PrefField(value = "show_weekday",boolDef = true)
    boolean showWeekday;
    @PrefField(value = "classes_of_day",numDef = 10)
    int classesOfDay;
    @PrefField(value = "weeks_of_term",numDef = 20)
    int weeksOfTerm = 20;

    public TimetableViewConfigData(String termBeginsTime, boolean showWeekday, int classesOfDay, int weeksOfTerm) {
        this.termBeginsTime = termBeginsTime;
        this.showWeekday = showWeekday;
        this.classesOfDay = classesOfDay;
        this.weeksOfTerm = weeksOfTerm;
    }

    public TimetableViewConfigData() {
    }
}
