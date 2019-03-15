package com.wp.csmu.classschedule.utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    public static final long MILLISECOND_OF_SECOND=1000L;
    public static final long MILLISECOND_OF_MINUTE=MILLISECOND_OF_SECOND*60;
    public static final long MILLISECOND_OF_HOUR=MILLISECOND_OF_MINUTE*60;
    public static final long MILLISECOND_OF_DAY=MILLISECOND_OF_HOUR*24;
    public static int getCurrentWeek(String startTime) {
        int length=0;
        try {
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
            Date start= simpleDateFormat.parse(startTime);
            Date current=new Date(System.currentTimeMillis());
            length=getBetweenDay(start,current);
        } catch (ParseException e) {
            e.printStackTrace();
        }finally {
            return length/7+1;
        }
    }

    public static int getBetweenDay(Date startDate,Date endDate) {
        Calendar calendar1=Calendar.getInstance();
        calendar1.setTime(startDate);
        calendar1.set(Calendar.HOUR_OF_DAY,0);
        calendar1.set(Calendar.MILLISECOND,0);
        calendar1.set(Calendar.MINUTE,0);
        calendar1.set(Calendar.SECOND,0);
        Calendar calendar2=Calendar.getInstance();
        calendar2.setTime(endDate);
        calendar2.set(Calendar.HOUR_OF_DAY,0);
        calendar2.set(Calendar.MILLISECOND,0);
        calendar2.set(Calendar.MINUTE,0);
        calendar2.set(Calendar.SECOND,0);
        return (int)((calendar2.getTime().getTime()-calendar1.getTime().getTime())/MILLISECOND_OF_DAY);
    }
}
