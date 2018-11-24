package com.wp.csmu.classschedule.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.wp.csmu.classschedule.R;
import com.wp.csmu.classschedule.application.MyApplication;
import com.wp.csmu.classschedule.network.NetWorkHelper;
import com.wp.csmu.classschedule.view.utils.BindView;
import com.zhuangfei.timetable.TimetableView;
import com.zhuangfei.timetable.listener.ISchedule;
import com.zhuangfei.timetable.view.WeekView;

public class MainActivity extends BaseActivity {
    @BindView(R.id.mainToolBar)
    Toolbar toolbar;
    @BindView(R.id.mainTimeTableView)
    TimetableView timetableView;
    @BindView(R.id.mainSwipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    Handler handler;
    AlertDialog loginDialog;

    String account = "";

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(toolbar);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1://登陆成功
                        loginDialog.getButton(Dialog.BUTTON_POSITIVE).setText("登陆");
                        loginDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
                        loginDialog.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(true);
                        loginDialog.dismiss();
                        loadScheduleAtFirst();
                        break;
                    case -1:
                        loginDialog.getButton(Dialog.BUTTON_POSITIVE).setText("登陆");
                        loginDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
                        loginDialog.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(true);
                        Exception exception = (Exception) msg.obj;
                        Toast.makeText(MyApplication.getContext(), "登陆失败\n" + exception.toString(), Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        timetableView.setSource(NetWorkHelper.subjects);
                        timetableView.setCurWeek(NetWorkHelper.weekIndex);
                        swipeRefreshLayout.setRefreshing(false);
                        timetableView.showView();
                        getSupportActionBar().setSubtitle("第 " + timetableView.getCurWeek() + " 周");
                        break;
                    case -2:
                        swipeRefreshLayout.setRefreshing(false);
                        Exception exception1 = (Exception) msg.obj;
                        Toast.makeText(MyApplication.getContext(), exception1.toString(), Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        timetableView.setSource(NetWorkHelper.subjects);
                        swipeRefreshLayout.setRefreshing(false);
                        timetableView.changeWeek(NetWorkHelper.weekIndex,false);
                        timetableView.updateView();
                        timetableView.updateDateView();
                        getSupportActionBar().setSubtitle("第 " + timetableView.getCurWeek() + " 周");
                }
            }
        };

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (timetableView.getCurWeek() == 0) {
                    loadScheduleAtFirst();
                } else {
                    loadScheduleByWeekIndex(timetableView.getCurWeek());
                }
            }
        });
        init();

    }

    private void init() {
        SharedPreferences sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        final String account = sharedPreferences.getString("account", null);
        final String password = sharedPreferences.getString("password", null);
        if (account == null || password == null) {
            //提示登陆
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            final View loginView = LayoutInflater.from(this).inflate(R.layout.layout_login_dialog, null);

            loginDialog = alertDialog.setTitle("登陆").setView(loginView).setCancelable(false)
                    .setPositiveButton("登陆", null).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    }).show();
            loginDialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //获取输入的用户名和密码
                    String inputAccount = ((TextInputLayout) loginView.findViewById(R.id.mainTextInputLayout1)).getEditText().getText().toString();
                    String inputPassword = ((TextInputLayout) loginView.findViewById(R.id.mainTextInputLayout2)).getEditText().getText().toString();
                    login(inputAccount, inputPassword);
                }
            });
        } else {
            MainActivity.this.account = account;
            NetWorkHelper.token = sharedPreferences.getString("token", null);
            loadScheduleAtFirst();
        }
    }

    private void login(final String account, final String password) {
        if (loginDialog != null) {
            loginDialog.getButton(Dialog.BUTTON_POSITIVE).setText("正在登陆");
            loginDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
            loginDialog.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(false);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    NetWorkHelper.login(account, password);
                    MainActivity.this.account = account;
                    handler.sendEmptyMessage(1);//登陆成功
                } catch (Exception e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.obj = e;
                    message.what = -1;
                    handler.sendMessage(message);
                }
            }
        }).start();
    }

    private void loadScheduleAtFirst() {
        swipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    NetWorkHelper.getSchedule(account, -1);
                    handler.sendEmptyMessage(2);
                } catch (Exception e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = -2;
                    message.obj = e;
                    handler.sendMessage(message);
                }
            }
        }).start();
    }

    private void loadScheduleByWeekIndex(final int weekIndex) {
        swipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    NetWorkHelper.getSchedule(account, weekIndex);
                    timetableView.setCurWeek(weekIndex);
                    handler.sendEmptyMessage(3);
                } catch (Exception e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = -2;
                    message.obj = e;
                    handler.sendMessage(message);
                }
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mainMenuPreviousWeek:
                loadScheduleByWeekIndex(timetableView.getCurWeek() - 1);
                break;
            case R.id.mainMenuNextWeek:
                loadScheduleByWeekIndex(timetableView.getCurWeek() + 1);
                break;
            case R.id.mainMenuGoToCurrent:
                loadScheduleAtFirst();
                break;
        }
        return true;
    }
}
