package com.wp.csmu.classschedule.view.scheduletable;

import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.model.ScheduleEnable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 表示首页的课程
 */
public class Subjects implements ScheduleEnable, Serializable {
    private static final long serialVersionUID = 0xabcdefL;
    //课程在哪一天，用1-7表示
    private int day;
    //课程名称
    private String name;
    //教室
    private String room;
    //从第几节课开始
    private int start;
    //持续几节课
    private int step;
    //老师名字
    private String teacher;
    //到第几节课结束
    private int end;
    //这门课在哪些周
    private List<Integer> weeks = new ArrayList<>();

    /**
     * 获取结束时间
     *
     * @return 结束时间
     */
    public int getEnd() {
        return end;
    }

    /**
     * 设置结束时间
     *
     * @param end 结束时间
     */
    public void setEnd(int end) {
        this.end = end;
    }

    /**
     * 获取所在周次
     *
     * @return 周次
     */
    public List<Integer> getWeeks() {
        return weeks;
    }

    /**
     * 设置所在周次
     *
     * @param weeks 周次
     */
    public void setWeeks(List<Integer> weeks) {
        this.weeks = weeks;
    }

    /**
     * 获取在哪一天
     *
     * @return 日期
     */
    public int getDay() {
        return day;
    }

    /**
     * 设置所在天
     *
     * @param day 所在天
     */
    public void setDay(int day) {
        this.day = day;
    }

    /**
     * 获取课程名称
     *
     * @return 课程名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置课程名称
     *
     * @param name 课程名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取教室
     *
     * @return 教室
     */
    public String getRoom() {
        return room;
    }

    /**
     * 设置教室
     *
     * @param room 教室
     */
    public void setRoom(String room) {
        this.room = room;
    }

    /**
     * 获取开始时间
     *
     * @return 开始时间
     */
    public int getStart() {
        return start;
    }

    /**
     * 设置开始时间
     *
     * @param start 开始时间
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * 获取持续时间
     *
     * @return 持续时间
     */
    public int getStep() {
        return step;
    }

    /**
     * 设置持续时间
     *
     * @param step 持续时间
     */
    public void setStep(int step) {
        this.step = step;
    }

    /**
     * 获取教师名字
     *
     * @return 教师名字
     */
    public String getTeacher() {
        return teacher;
    }

    /**
     * 获取教师名字
     *
     * @param teacher 教师名字
     */
    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    /**
     * 继承自 {@link com.zhuangfei.timetable.model.ScheduleEnable }
     * 主要是把这个自定义类转为Schedule
     *
     * @return 标准的Schedulw
     */
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

    /**
     * 计算hash码
     * 将这里面的字段按顺序拼接为字符串后，返回这个字符串的hash码
     *
     * @return hash码
     */
    @Override
    public int hashCode() {
        return (day + name + room + start + step + teacher + end + list2String(weeks)).hashCode();
    }

    /**
     * 判断两个类是否相等
     * 依次判断这两个类中的所有字段的值
     *
     * @param obj 需要比较的类
     * @return 结果
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Subjects)) {
            return false;
        }
        Subjects subjects = (Subjects) obj;
        return this.day == subjects.getDay() &&
                this.end == subjects.getEnd() &&
                this.name.equals(subjects.getName()) &&
                this.room.equals(subjects.getRoom()) &&
                this.start == subjects.getStart() &&
                this.step == subjects.getStep() &&
                this.teacher.equals(subjects.getTeacher()) &&
                this.weeks.equals(subjects.getWeeks());
    }

    /**
     * 将List转换为字符串
     * 将其中的所有值简单连接起来
     *
     * @param list 需要转换的List
     * @return 字符串
     */
    private String list2String(List<Integer> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i : list) {
            stringBuilder.append(i).append(",");
        }
        return stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length()).toString();
    }
}
