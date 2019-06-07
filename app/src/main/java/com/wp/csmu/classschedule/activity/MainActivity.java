package com.wp.csmu.classschedule.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.wp.csmu.classschedule.R;
import com.wp.csmu.classschedule.io.IO;
import com.wp.csmu.classschedule.network.LoginHelper;
import com.wp.csmu.classschedule.utils.DateUtils;
import com.wp.csmu.classschedule.view.scheduletable.Subjects;
import com.wp.csmu.classschedule.view.utils.BindView;
import com.zhuangfei.timetable.TimetableView;
import com.zhuangfei.timetable.listener.OnItemClickAdapter;
import com.zhuangfei.timetable.model.Schedule;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends BaseActivity {
    HashSet<Subjects> subjects;
    int currentWeek;
    int targetWeek;
    @BindView(R.id.mainSwipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.mainCoordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.mainToolBar)
    Toolbar toolbar;
    @BindView(R.id.mainTimeTableView)
    TimetableView timetableView;
    @BindView(R.id.mainNavigationView)
    NavigationView navigationView;
    @BindView(R.id.mainDrawerLayout)
    DrawerLayout drawerLayout;

    String termBeginsTime;
    boolean showWeekday;
    int classesOfDay;
    int weeksOfTerm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(toolbar);
        SharedPreferences sharedPreferences = getSharedPreferences("com.wp.csmu.classschedule_preferences", MODE_PRIVATE);
        termBeginsTime = sharedPreferences.getString("term_begins_time", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        showWeekday = sharedPreferences.getBoolean("show_weekday", true);
        classesOfDay = sharedPreferences.getInt("classes_of_day", 10);
        weeksOfTerm = sharedPreferences.getInt("weeks_of_term", Math.max(20, currentWeek));

        targetWeek = currentWeek = DateUtils.getCurrentWeek(termBeginsTime);
        timetableView.isShowWeekends(showWeekday);
        timetableView.maxSlideItem(classesOfDay);

        getSupportActionBar().setSubtitle("第"+currentWeek+"周");
        if (checkSchedule()){
            readSchedule();
        }else {
            importSchedule();
        }
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.mainDrawerHomePage:
                        startActivity(new Intent(MainActivity.this,HomepageActivity.class));
                        break;
                    case R.id.mainDrawerScore:
                        startActivity(new Intent(MainActivity.this,ScoreActivity.class));
                        break;
                    case R.id.mainDrawerRefresh:
                        refreshSchedule();
                        break;
                    case R.id.mainDrawerSetting:
                        startActivity(new Intent(MainActivity.this, SettingActivity.class));
                        break;
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });
        timetableView.callback(new OnItemClickAdapter(){
            @Override
            public void onItemClick(View v, List<Schedule> scheduleList) {
                for (Schedule schedule:scheduleList){
                    if (schedule.getWeekList().contains(Integer.valueOf(toolbar.getSubtitle().toString().substring(1,toolbar.getSubtitle().toString().length()-1))))
                    {
                        Log.i("课程",schedule.getName()+"\t"+schedule.getTeacher());
                        showScheduleInfo(schedule);
                        break;
                    }
                }
            }
        });
    }

    boolean checkSchedule() {
        //检查开学时间是否存在
        SharedPreferences sharedPreferences = getSharedPreferences("com.wp.csmu.classschedule_preferences", MODE_PRIVATE);
        File file = new File(IO.scheduleFile);
        return file.exists() && sharedPreferences.contains("term_begins_time");
    }

    void importSchedule(){
        swipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences sharedPreferences=getSharedPreferences("user",MODE_PRIVATE);
                    LoginHelper.getSchedule(sharedPreferences.getString("account","null"),sharedPreferences.getString("password","null"));
                    IO.writeSchedule(LoginHelper.getSchedules());
                    SharedPreferences sharedPreferences1 = getSharedPreferences("com.wp.csmu.classschedule_preferences", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences1.edit();
                    editor.putString("term_begins_time", LoginHelper.getTermBeginsTime());
                    editor.commit();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                            readSchedule();
                            timetableView.curWeek(currentWeek);
                            timetableView.source(new ArrayList<>(subjects));
                            timetableView.showView();
                        }
                    });
                }catch (final Exception e){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(true);
                            Snackbar.make(coordinatorLayout,"导入失败\n"+e.toString(),Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    void readSchedule(){
        try {
            subjects=IO.readSchedule();
            timetableView.curWeek(currentWeek);
            timetableView.source(new ArrayList<>(subjects));
            timetableView.showView();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.mainMenuNextWeek:
                if (targetWeek == weeksOfTerm) {
                    break;
                }
                timetableView.changeWeekForce(++this.targetWeek);
                break;
            case R.id.mainMenuPreviousWeek:
                if (targetWeek == 1) {
                    break;
                }
                timetableView.changeWeekForce(--this.targetWeek);
                break;
            case R.id.mainMenuGoToCurrent:
                timetableView.changeWeekForce(this.targetWeek=currentWeek);
                break;
        }
        timetableView.showView();
        timetableView.onDateBuildListener().onUpdateDate(currentWeek,targetWeek);
        Log.i("MainActivity","菜单被选择");
        getSupportActionBar().setSubtitle("第"+targetWeek+"周");
        return true;
    }

    private void showScheduleInfo(Schedule schedule){
        String name=schedule.getName();
        String room=schedule.getRoom();
        String teacher=schedule.getTeacher();
        List<Integer>weekList=schedule.getWeekList();
        StringBuilder stringBuilder=new StringBuilder();
        for (int i=0;i<weekList.size();i++){
            stringBuilder.append(weekList.get(i));
            if (i!=weekList.size()-1){
                stringBuilder.append(", ");
            }else {
                stringBuilder.append(" 周");
            }
        }
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(name);
        View view= LayoutInflater.from(this).inflate(R.layout.schedule_info_dialog,null);
        TextView t1=view.findViewById(R.id.scheduleInfoTextView1);
        TextView t2=view.findViewById(R.id.scheduleInfoTextView2);
        TextView t3=view.findViewById(R.id.scheduleInfoTextView3);
        t1.setText(stringBuilder);
        t2.setText(teacher);
        t3.setText(room);
        builder.setView(view);
        builder.create().show();
    }

    private void refreshSchedule(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("刷新课程表？（需要网络）").setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reloadSchedule();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }

    private void reloadSchedule(){
        SharedPreferences sharedPreferences=getSharedPreferences("user",MODE_PRIVATE);
        final String account=sharedPreferences.getString("account","");
        final String password=sharedPreferences.getString("password","");
        swipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LoginHelper.getSchedule(account,password);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                            readSchedule();
                            timetableView.curWeek(currentWeek);
                            timetableView.source(new ArrayList<>(subjects));
                            timetableView.showView();
                            Snackbar.make(coordinatorLayout,"刷新成功",Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }catch (Exception e){
                    swipeRefreshLayout.setRefreshing(false);
                    Snackbar.make(coordinatorLayout,"刷新失败\n"+e.toString(),Snackbar.LENGTH_SHORT).show();
                }
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("com.wp.csmu.classschedule_preferences", MODE_PRIVATE);
        termBeginsTime = sharedPreferences.getString("term_begins_time", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        showWeekday = sharedPreferences.getBoolean("show_weekday", true);
        classesOfDay = sharedPreferences.getInt("classes_of_day", 10);
        weeksOfTerm = sharedPreferences.getInt("weeks_of_term", Math.max(20, currentWeek));

        targetWeek = currentWeek = DateUtils.getCurrentWeek(termBeginsTime);
        timetableView.isShowWeekends(showWeekday);
        timetableView.maxSlideItem(classesOfDay);
        timetableView.updateView();
    }
}