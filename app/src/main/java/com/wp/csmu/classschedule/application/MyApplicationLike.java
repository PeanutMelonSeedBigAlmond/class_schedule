package com.wp.csmu.classschedule.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;

import androidx.multidex.MultiDex;

import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.tinker.entry.DefaultApplicationLike;
import com.wp.csmu.classschedule.BuildConfig;
import com.wp.csmu.classschedule.data.sharedpreferences.TimetableViewConfigData;
import com.wp.csmu.classschedule.data.sharedpreferences.User;

import net.nashlegend.anypref.AnyPref;

public class MyApplicationLike extends DefaultApplicationLike {

    public static final String fullClassName = MyApplicationLike.class.getName();
    public static Context context;
    public static TimetableViewConfigData configData;
    public static User user;

    public MyApplicationLike(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);
    }

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplication();
        if (!isDebug()) {
            Bugly.init(context, BuildConfig.APPID, true);
        }

        AnyPref.init(context);
        configData = AnyPref.get(TimetableViewConfigData.class);
        user = AnyPref.get(User.class);

        if (isDebug() && !com.squareup.leakcanary.LeakCanary.isInAnalyzerProcess(context)) {
            com.squareup.leakcanary.LeakCanary.install(getApplication());
        }
    }

    private boolean isDebug() {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);
        // you must install multiDex whatever tinker is installed!
        MultiDex.install(base);
        // 安装tinker
        Beta.installTinker(this);
    }

    public void registerActivityLifecycleCallback(Application.ActivityLifecycleCallbacks callbacks) {
        getApplication().registerActivityLifecycleCallbacks(callbacks);
    }
}
