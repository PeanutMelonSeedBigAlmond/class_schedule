package com.wp.csmu.classschedule.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.wp.csmu.classschedule.R;
import com.wp.csmu.classschedule.config.TimetableViewConfig;
import com.wp.csmu.classschedule.fragment.ScheduleFragment;
import com.wp.csmu.classschedule.io.IO;
import com.wp.csmu.classschedule.network.NetworkHelper;
import com.wp.csmu.classschedule.utils.DateUtils;
import com.wp.csmu.classschedule.view.adapter.ScheduleViewPagerAdapter;
import com.wp.csmu.classschedule.view.scheduletable.AppSubjects;
import com.wp.csmu.classschedule.view.utils.BindView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends BaseActivity {
    int currentWeek;
    @BindView(R.id.mainSwipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.mainCoordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.mainToolBar)
    Toolbar toolbar;
    @BindView(R.id.mainNavigationView)
    NavigationView navigationView;
    @BindView(R.id.mainDrawerLayout)
    DrawerLayout drawerLayout;
    @BindView(R.id.mainViewPager)
    ViewPager viewPager;

    List<ScheduleFragment> fragments;
    FragmentManager fragmentManager;
    ScheduleViewPagerAdapter adapter;

    int lastWeeksOfTerm;
    long lastClickTime = -1500;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime > 500) {
                    lastClickTime = currentTime;
                } else {
                    viewPager.setCurrentItem(currentWeek - 1);
                    lastClickTime = currentTime;
                }
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("com.wp.csmu.classschedule_preferences", MODE_PRIVATE);

        TimetableViewConfig.INSTANCE.setTermBeginsTime(sharedPreferences.getString("term_begins_time", new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
        TimetableViewConfig.INSTANCE.setShowWeekday(sharedPreferences.getBoolean("show_weekday", true));
        TimetableViewConfig.INSTANCE.setClassesOfDay(sharedPreferences.getInt("classes_of_day", 10));
        TimetableViewConfig.INSTANCE.setWeeksOfTerm(lastWeeksOfTerm = sharedPreferences.getInt("weeks_of_term", Math.max(20, currentWeek)));
        currentWeek = DateUtils.getCurrentWeek(TimetableViewConfig.INSTANCE.getTermBeginsTime());

        fragments = new ArrayList<>();
        for (int i = 1; i <= TimetableViewConfig.INSTANCE.getWeeksOfTerm(); i++) {
            fragments.add(ScheduleFragment.Companion.newInstance(i));
        }
        fragmentManager = getSupportFragmentManager();
        adapter = new ScheduleViewPagerAdapter(fragmentManager, fragments);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new MyOnPageChangeListener());
        viewPager.setOffscreenPageLimit(2);

        if (checkSchedule()) {
            readSchedule();
        } else {
            importSchedule();
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.mainDrawerScore:
                        startActivity(new Intent(MainActivity.this, ScoreActivity.class));
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

    }

    boolean checkSchedule() {
        //检查开学时间是否存在
        SharedPreferences sharedPreferences = getSharedPreferences("com.wp.csmu.classschedule_preferences", MODE_PRIVATE);
        File file = new File(IO.scheduleFile);
        return file.exists() && sharedPreferences.contains("term_begins_time");
    }

    void importSchedule() {
        swipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                    NetworkHelper.GetSchedule.getSchedule(sharedPreferences.getString("account", "null"), sharedPreferences.getString("password", "null"));
                    IO.writeSchedule(NetworkHelper.getSchedules());
                    SharedPreferences sharedPreferences1 = getSharedPreferences("com.wp.csmu.classschedule_preferences", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences1.edit();
                    editor.putString("term_begins_time", NetworkHelper.getTermBeginsTime());
                    editor.commit();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                            readSchedule();
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(true);
                            Snackbar.make(coordinatorLayout, "导入失败\n" + e.toString(), Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    void readSchedule() {
        try {
            AppSubjects.Companion.setSubjects(IO.readSchedule());
            viewPager.setCurrentItem(currentWeek - 1);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.gotoWeek:
                View view = LayoutInflater.from(this).inflate(R.layout.weeks_classes_selector, null);
                final TextView t1, t2, t3;
                final SeekBar sb;
                t1 = view.findViewById(R.id.weeksClassesSelectorTextView1);
                t2 = view.findViewById(R.id.weeksClassesSelectorTextView2);
                t3 = view.findViewById(R.id.weeksClassesSelectorTextView3);
                sb = view.findViewById(R.id.weeksClassesSelectorSeekBar);
                sb.setProgress(viewPager.getCurrentItem());
                sb.setMax(fragments.size() - 1);
                t1.setText(sb.getProgress() + 1 + "");
                t2.setText("1");
                t3.setText(sb.getMax() + 1 + "");
                sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        t1.setText(progress + 1 + "");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("跳转").setView(view).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        viewPager.setCurrentItem(sb.getProgress());
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
        }
        return true;
    }

    private void refreshSchedule() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    private void reloadSchedule() {
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        final String account = sharedPreferences.getString("account", "");
        final String password = sharedPreferences.getString("password", "");
        swipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    NetworkHelper.GetSchedule.getSchedule(account, password);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                            readSchedule();
                            Snackbar.make(coordinatorLayout, "刷新成功", Snackbar.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    swipeRefreshLayout.setRefreshing(false);
                    Snackbar.make(coordinatorLayout, "刷新失败\n" + e.toString(), Snackbar.LENGTH_SHORT).show();
                }
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("com.wp.csmu.classschedule_preferences", MODE_PRIVATE);

        TimetableViewConfig.INSTANCE.setTermBeginsTime(sharedPreferences.getString("term_begins_time", new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
        TimetableViewConfig.INSTANCE.setShowWeekday(sharedPreferences.getBoolean("show_weekday", true));
        TimetableViewConfig.INSTANCE.setClassesOfDay(sharedPreferences.getInt("classes_of_day", 10));
        TimetableViewConfig.INSTANCE.setWeeksOfTerm(sharedPreferences.getInt("weeks_of_term", Math.max(20, currentWeek)));
        currentWeek = DateUtils.getCurrentWeek(TimetableViewConfig.INSTANCE.getTermBeginsTime());
        if (lastWeeksOfTerm != TimetableViewConfig.INSTANCE.getWeeksOfTerm()) {
            fragments.clear();
            for (int i = 1; i <= TimetableViewConfig.INSTANCE.getWeeksOfTerm(); i++) {
                fragments.add(ScheduleFragment.Companion.newInstance(i));
            }
            adapter.fragmentChanged(fragments);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            getSupportActionBar().setSubtitle("第" + (position + 1) + "周");
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }
}