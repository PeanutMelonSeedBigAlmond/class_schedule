package com.wp.csmu.classschedule.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.wp.csmu.classschedule.R;

public class SplashActivity extends AppCompatActivity {
    Handler handle=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch(msg.what){
                case 0:
                    startActivity(new Intent(SplashActivity.this,MainActivity.class));
                    break;
                case 1:
                    startActivity(new Intent(SplashActivity.this,LoginActivity.class));
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
        if (!checkAccount()){
            flag=1;
        }
        handle.sendEmptyMessageDelayed(flag,1500);
    }

    private boolean checkAccount(){
        SharedPreferences sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        final String account = sharedPreferences.getString("account", null);
        final String password = sharedPreferences.getString("password", null);
        return account == null || password == null;
    }
}
