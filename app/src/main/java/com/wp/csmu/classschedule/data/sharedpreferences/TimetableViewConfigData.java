package com.wp.csmu.classschedule.data.sharedpreferences;

import com.wp.csmu.classschedule.utils.DateUtils;

import net.nashlegend.anypref.annotations.PrefField;
import net.nashlegend.anypref.annotations.PrefModel;

@PrefModel("com.wp.csmu.classschedule_preferences")
public class TimetableViewConfigData {
    @PrefField(value = "term_begins_time", strDef = "")
    public String termBeginsTime = "";
    @PrefField(value = "show_weekday", boolDef = true)
    public boolean showWeekday = true;
    @PrefField(value = "classes_of_day", numDef = 10)
    public int classesOfDay = 10;
    @PrefField(value = "weeks_of_term", numDef = 20)
    public int weeksOfTerm = 20;
    @PrefField(value = "current_term_id", strDef = "")
    public String currentTermId;

    public TimetableViewConfigData(String termBeginsTime, boolean showWeekday, int classesOfDay, int weeksOfTerm, String currentTermId) {
        this.termBeginsTime = termBeginsTime;
        this.showWeekday = showWeekday;
        this.classesOfDay = classesOfDay;
        this.weeksOfTerm = weeksOfTerm;
        this.currentTermId = currentTermId;
    }

    public TimetableViewConfigData() {
    }

    public String getTermBeginsTime() {
        return (termBeginsTime == null || "".equals(termBeginsTime)) ? DateUtils.getCurrentDate() : termBeginsTime;
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

    public String getCurrentTermId() {
        return currentTermId;
    }

    public void setCurrentTermId(String currentTermId) {
        this.currentTermId = currentTermId;
    }
}
