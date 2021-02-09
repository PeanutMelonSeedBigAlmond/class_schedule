package com.wp.csmu.classschedule.application;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;

import androidx.multidex.MultiDex;

import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.wp.csmu.classschedule.BuildConfig;
import com.wp.csmu.classschedule.data.sharedpreferences.TimetableViewConfigData;
import com.wp.csmu.classschedule.data.sharedpreferences.User;

import net.nashlegend.anypref.AnyPref;

public final class MyApplication extends Application {
    public static Context context;

    public static TimetableViewConfigData configData;

    public static User user;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        if (!isDebug()) {
            Bugly.init(context, BuildConfig.APPID, true);
        }

        AnyPref.init(this);
        configData = AnyPref.get(TimetableViewConfigData.class);
        user = AnyPref.get(User.class);

        if (isDebug() && !com.squareup.leakcanary.LeakCanary.isInAnalyzerProcess(context)) {
            com.squareup.leakcanary.LeakCanary.install(this);
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
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // you must install multiDex whatever tinker is installed!
        MultiDex.install(base);
        // 安装tinker
        Beta.installTinker();
    }
}
