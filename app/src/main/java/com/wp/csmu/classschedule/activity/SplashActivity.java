package com.wp.csmu.classschedule.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

import androidx.appcompat.app.AppCompatActivity;

import com.wp.csmu.classschedule.R;
import com.wp.csmu.classschedule.activity.loginactivity.LoginActivity;
import com.wp.csmu.classschedule.activity.mainactivity.MainActivity;
import com.wp.csmu.classschedule.data.sharedpreferences.User;
import com.wp.csmu.classschedule.io.IO;

import net.nashlegend.anypref.AnyPref;

import org.jetbrains.annotations.NotNull;

public class SplashActivity extends AppCompatActivity {
    Handler handle=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NotNull Message msg) {
            switch(msg.what){
                case 0:
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    break;
                case 1:
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    break;
            }
            finish();
            return true;
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        int flag=0;
        if (!checkAccount()||!checkData()){
            flag=1;
        }
        handle.sendEmptyMessageDelayed(flag,300);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean checkAccount(){
        User user= AnyPref.get(User.class);
        return !("".equals(user.getAccount()) || "".equals(user.getPassword()));
    }

    private boolean checkData(){
        try {
            IO.readSchedule();
        }catch (Exception e){
            return false;
        }
        return true;
    }
}
