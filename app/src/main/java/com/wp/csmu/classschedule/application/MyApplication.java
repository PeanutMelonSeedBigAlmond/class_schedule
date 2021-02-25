package com.wp.csmu.classschedule.application;

import android.util.Log;

import com.tencent.tinker.loader.app.TinkerApplication;
import com.tencent.tinker.loader.shareutil.ShareConstants;

public final class MyApplication extends TinkerApplication {
    public MyApplication() {
        super(
                ShareConstants.TINKER_ENABLE_ALL,
                MyApplicationLike.fullClassName
        );
        Log.e("Class Name",MyApplicationLike.fullClassName);
    }
}
