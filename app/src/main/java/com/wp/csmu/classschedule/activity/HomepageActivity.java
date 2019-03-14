package com.wp.csmu.classschedule.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.wp.csmu.classschedule.R;
import com.wp.csmu.classschedule.network.NetWorkHelper;
import com.wp.csmu.classschedule.view.utils.BindView;

public class HomepageActivity extends BaseActivity {
    @BindView(R.id.homepageToolbar)
    Toolbar toolbar;
    @BindView(R.id.homepageSwipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.homepageCoordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.homepageTvName)
    TextView name;
    @BindView(R.id.homepageTvNumber)
    TextView number;
    @BindView(R.id.homepageTvSex)
    TextView sex;
    @BindView(R.id.homepageTvTelephone)
    TextView telephone;
    @BindView(R.id.homepageTvQQ)
    TextView qq;
    @BindView(R.id.homepageTvEmail)
    TextView email;
    @BindView(R.id.homepageTvDepartment)
    TextView department;
    @BindView(R.id.homepageTvMajor)
    TextView major;
    @BindView(R.id.homepageTvClass)
    TextView classTv;
    @BindView(R.id.homepageTvEntranceTime)
    TextView entranceTime;
    @BindView(R.id.homepageTvGrade)
    TextView grade;
    @BindView(R.id.homepageTvSeconeMajor)
    TextView secondMajor;
    @BindView(R.id.logoutButton)
    Handler handler;
    String[] info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case -1:
                        swipeRefreshLayout.setRefreshing(false);
                        Exception exception = (Exception) msg.obj;
                        Snackbar.make(coordinatorLayout, "出现错误：\n" + exception.toString(), Snackbar.LENGTH_SHORT).show();
                        break;
                    case 0:
                        swipeRefreshLayout.setRefreshing(false);
                        name.setText(info[0]);
                        number.setText(info[1]);
                        sex.setText(info[2]);
                        telephone.setText(info[3]);
                        qq.setText(info[4]);
                        email.setText(info[5]);
                        department.setText(info[6]);
                        major.setText(info[7]);
                        classTv.setText(info[8]);
                        entranceTime.setText(info[9]);
                        grade.setText(info[10]);
                        secondMajor.setText(info[11]);
                        break;
                }
                return true;
            }
        });

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getHomepage();
            }
        });
        getHomepage();
    }

    void getHomepage() {
        swipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                String account = sharedPreferences.getString("account", "");
                try {
                    info = NetWorkHelper.getHomepage(account);
                    handler.sendEmptyMessage(0);
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = -1;
                    msg.obj = e;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    public void logout(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this).setMessage("确定退出登录吗").setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.commit();
                MainActivity.showDialog = true;
                finish();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show();
    }
}
