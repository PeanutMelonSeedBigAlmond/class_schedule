package com.wp.csmu.classschedule.view.scheduletable;

import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.model.ScheduleEnable;


import java.util.ArrayList;
import java.util.List;

public class Subjects implements ScheduleEnable {
    int day;
    String name;
    String room;
    int start;
    int step;
    String teacher;

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    int end;

    public List<Integer> getWeeks() {
        return weeks;
    }

    public void setWeeks(List<Integer> weeks) {
        this.weeks = weeks;
    }

    List<Integer> weeks = new ArrayList<>();

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    @Override
    public Schedule getSchedule() {
        Schedule schedule = new Schedule();
        schedule.setColorRandom(2);
        schedule.setDay(day);
        schedule.setName(name);
        schedule.setRoom(room);
        schedule.setStart(start);
        schedule.setTeacher(teacher);
        schedule.setStep(step);
        schedule.setWeekList(weeks);
        return schedule;
    }
}
