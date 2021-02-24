package com.wp.csmu.classschedule.view.utils;

import android.app.Activity;

import java.lang.reflect.Field;

public class BindViewHelper {
    /**
     * 绑定控件时初始化view
     *
     * @param activity 要初始化的eactivity
     */
    public static void initView(Activity activity) {
        //获取类

        Class<? extends Activity> recfClass = activity.getClass();

        //获取类中的所有field（域/变量）

        Field[] fields = recfClass.getDeclaredFields();

        //对获取到的field做遍历

        for (Field field : fields) {

            //对遍历到的field做判断，是否带特定注解标识

            if (field.isAnnotationPresent(BindView.class)) {

                //获取到该field的注解

                BindView bindView = field.getAnnotation(BindView.class);

                //获取到该field的注解的value

                int id = bindView.value();

                //设置属性

                field.setAccessible(true);

                try {

                    //对该field做控件绑定操作

                    field.set(activity, activity.findViewById(id));

                } catch (IllegalAccessException | IllegalArgumentException e) {

                    e.printStackTrace();

                }

            }


        }
    }
}
