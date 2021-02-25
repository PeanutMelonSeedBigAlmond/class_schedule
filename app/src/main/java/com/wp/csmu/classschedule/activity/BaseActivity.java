package com.wp.csmu.classschedule.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.wp.csmu.classschedule.view.utils.BindViewHelper;

import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity {
    private static final List<AppCompatActivity> activities = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activities.add(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        BindViewHelper.initView(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activities.remove(this);
    }

    public final void finishAllActivity() {
        for (AppCompatActivity activity : activities) {
            activity.finish();
        }
    }

    public final void finishActivity(AppCompatActivity activity) {
        if (activities.contains(activity)) {
            activity.finish();
        }
    }
}
