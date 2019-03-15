package com.wp.csmu.classschedule.view.scheduletable;

import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.model.ScheduleEnable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Subjects implements ScheduleEnable, Serializable {
    private int day;
    private String name;
    private String room;
    private int start;
    private int step;
    private String teacher;
    private int end;
    private List<Integer> weeks = new ArrayList<>();
    private static final long serialVersionUID=0xabcdefL;
    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public List<Integer> getWeeks() {
        return weeks;
    }

    public void setWeeks(List<Integer> weeks) {
        this.weeks = weeks;
    }

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
