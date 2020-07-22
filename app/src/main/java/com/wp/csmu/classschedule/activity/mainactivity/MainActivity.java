package com.wp.csmu.classschedule.activity.mainactivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.wp.csmu.classschedule.R;
import com.wp.csmu.classschedule.activity.BaseActivity;
import com.wp.csmu.classschedule.activity.ScoreActivity;
import com.wp.csmu.classschedule.activity.SettingActivity;
import com.wp.csmu.classschedule.config.TimetableViewConfig;
import com.wp.csmu.classschedule.fragment.ScheduleFragment;
import com.wp.csmu.classschedule.io.IO;
import com.wp.csmu.classschedule.network.Config;
import com.wp.csmu.classschedule.network.DataClient;
import com.wp.csmu.classschedule.network.LoginClient;
import com.wp.csmu.classschedule.utils.DateUtils;
import com.wp.csmu.classschedule.view.adapter.ScheduleViewPagerAdapter;
import com.wp.csmu.classschedule.view.scheduletable.AppSubjects;
import com.wp.csmu.classschedule.view.utils.BindView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    @BindView(R.id.mainConstraintLayout)
    ConstraintLayout constraintLayout;
    @BindView(R.id.appBarLayout)
    AppBarLayout appBarLayout;

    List<ScheduleFragment> fragments;
    FragmentManager fragmentManager;
    ScheduleViewPagerAdapter adapter;

    int lastWeeksOfTerm;
    long lastClickTime = -1500;
    boolean backgroundExists = false;

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

        //更改背景
        setBackground();

        SharedPreferences sharedPreferences = getSharedPreferences("com.wp.csmu.classschedule_preferences", MODE_PRIVATE);

        TimetableViewConfig.INSTANCE.setTermBeginsTime(sharedPreferences.getString("term_begins_time", new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
        TimetableViewConfig.INSTANCE.setShowWeekday(sharedPreferences.getBoolean("show_weekday", true));
        TimetableViewConfig.INSTANCE.setClassesOfDay(sharedPreferences.getInt("classes_of_day", 10));
        TimetableViewConfig.INSTANCE.setWeeksOfTerm(lastWeeksOfTerm = sharedPreferences.getInt("weeks_of_term", Math.max(20, currentWeek)));
        currentWeek = DateUtils.getCurrentWeek(TimetableViewConfig.INSTANCE.getTermBeginsTime());
        currentWeek = currentWeek <= 0 ? 1 : currentWeek;
        getSupportActionBar().setSubtitle("第" + currentWeek + "周");

        fragments = new ArrayList<>();
        for (int i = 1; i <= TimetableViewConfig.INSTANCE.getWeeksOfTerm(); i++) {
            fragments.add(ScheduleFragment.Companion.newInstance(i));
        }
        fragmentManager = getSupportFragmentManager();
        adapter = new ScheduleViewPagerAdapter(fragmentManager, fragments);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new MyOnPageChangeListener());

        readSchedule();
//        reloadSchedule("");

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
                    case R.id.mainDrawerAbout:
                        try {
                            InputStream inputStream = getAssets().open("about.html");
                            String text = IO.readString(inputStream);
                            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.about_dialog_layout, null, false);
                            TextView textView = view.findViewById(R.id.aboutDialogTextView);
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                //api version > 24 (android n)
                                textView.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT));
                            } else {
                                textView.setText(Html.fromHtml(text));
                            }
                            textView.setMovementMethod(LinkMovementMethod.getInstance());
                            builder.setTitle("关于").setView(view).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

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

    void readSchedule() {
        try {
            AppSubjects.Companion.setSubjects(IO.readSchedule());
            currentWeek = DateUtils.getCurrentWeek(TimetableViewConfig.INSTANCE.getTermBeginsTime());
            currentWeek = currentWeek < 0 ? 1 : currentWeek;
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
        switch (item.getItemId()) {
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
                reloadSchedule("");
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }

    private void reloadSchedule(String verifyCode) {
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        final String account = sharedPreferences.getString("account", "");
        final String password = sharedPreferences.getString("password", "");
        swipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (Config.INSTANCE.getState() == LoginClient.State.SUCCESS) {
                        DataClient.INSTANCE.getSchedule();
                    } else {
                        LoginClient.State state = LoginClient.INSTANCE.login(account, password, verifyCode);
                        switch (state) {
                            case SUCCESS:
                                IO.writeSchedule(DataClient.INSTANCE.getSchedule());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        readSchedule();
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                                break;
                            case NEED_VERIFY_CODE:
                                throw new Exception("need verify code");
                            case WRONG_PASSWORD:
                                throw new Exception("wrong password");
                            default:
                                break;
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                swipeRefreshLayout.setRefreshing(false);
                                IO.writeSchedule(Config.getSchedules());
                                SharedPreferences sharedPreferences1 = getSharedPreferences("com.wp.csmu.classschedule_preferences", MODE_PRIVATE);
                                SharedPreferences.Editor editor1 = sharedPreferences1.edit();
                                editor1.putString("term_begins_time", Config.getTermBeginsTime());
                                editor1.commit();
                                readSchedule();
                                adapter.notifyDataSetChanged();
                                Snackbar.make(coordinatorLayout, "刷新成功", Snackbar.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                                swipeRefreshLayout.setRefreshing(false);
                                Snackbar.make(coordinatorLayout, "刷新失败", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (IOException e) {
                    runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        if (checkSchedule()) {
                            Snackbar.make(coordinatorLayout, "网络连接不可用,已使用本地缓存", Snackbar.LENGTH_SHORT).show();
                            readSchedule();
                        } else {
                            Snackbar.make(coordinatorLayout, "网络连接不可用", Snackbar.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        switch (e.getMessage()) {
                            case "wrong password":
                                break;
                            case "need verify code":
                                showVerifyCode();
                                break;
                            default:
                                break;
                        }
                        e.printStackTrace();
                        Snackbar.make(coordinatorLayout, "刷新失败\n" + e.toString(), Snackbar.LENGTH_SHORT).show();
                    });
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

    private void setBackground() {
        File background = new File(IO.backgroundImg);
        if (backgroundExists = background.exists()) {
            try {
                FileInputStream fis = new FileInputStream(background);
                Bitmap bitmap = BitmapFactory.decodeStream(fis);
                constraintLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
                fis.close();
            } catch (Exception e) {

            }
        }
    }

    private void refreshVerifyCode(ImageView imageView) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LoginClient.INSTANCE.downloadVerifyCode();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = BitmapFactory.decodeFile(IO.verifyCodeImg);
                        imageView.setImageBitmap(bitmap);
                    }
                });
            }
        }).start();
    }

    private void showVerifyCode() {
        View view = LayoutInflater.from(this).inflate(R.layout.show_verify_code, null);
        refreshVerifyCode(view.findViewById(R.id.verifyCodeImage));
        (view.findViewById(R.id.refreshVerifyCode)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshVerifyCode(view.findViewById(R.id.verifyCodeImage));
            }
        });
        AlertDialog.Builder dialog = new AlertDialog.Builder(this).setView(view).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reloadSchedule(((TextInputLayout) view.findViewById(R.id.textInputLayout5)).getEditText().getText().toString().trim());
            }
        });
        dialog.show();
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