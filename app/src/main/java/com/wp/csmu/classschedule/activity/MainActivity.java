package com.wp.csmu.classschedule.activity;

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

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    ArrayList<Subjects> subjects;
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
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(toolbar);
        targetWeek=currentWeek= DateUtils.getCurrentWeek("2019-2-25");
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
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });
        timetableView.callback(new OnItemClickAdapter(){
            @Override
            public void onItemClick(View v, List<Schedule> scheduleList) {
                showScheduleInfo(scheduleList.get(0));
            }
        });
    }

    boolean checkSchedule() {
        File file = new File(IO.scheduleFile);
        return file.exists();
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                            readSchedule();
                            timetableView.curWeek(currentWeek);
                            timetableView.source(subjects);
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
            timetableView.source(subjects);
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
//        String name=schedule.getName();
//        String room=schedule.getRoom();
//        String teacher=schedule.getTeacher();
//        List<Integer>weekList=schedule.getWeekList();
//        StringBuilder stringBuilder=new StringBuilder();
//        for (int i=0;i<weekList.size();i++){
//            stringBuilder.append(weekList.get(i));
//            if (i!=weekList.size()-1){
//                stringBuilder.append(", ");
//            }else {
//                stringBuilder.append(" 周");
//            }
//        }
//        AlertDialog.Builder builder=new AlertDialog.Builder(this);
//        builder.setTitle(name);
//        View view= LayoutInflater.from(this).inflate(R.layout.schedule_info_dialog,null);
//        TextView t1=view.findViewById(R.id.scheduleInfoTextView1);
//        TextView t2=view.findViewById(R.id.scheduleInfoTextView2);
//        TextView t3=view.findViewById(R.id.scheduleInfoTextView3);
//        t1.setText(stringBuilder);
//        t2.setText(teacher);
//        t3.setText(room);
//        builder.setView(view);
//        builder.create().show();
    }
}