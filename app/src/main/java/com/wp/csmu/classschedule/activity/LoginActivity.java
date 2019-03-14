package com.wp.csmu.classschedule.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.wp.csmu.classschedule.R;
import com.wp.csmu.classschedule.network.LoginHelper;
import com.wp.csmu.classschedule.view.utils.BindView;

import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class LoginActivity extends BaseActivity {
    @BindView(R.id.loginTextInputLayout1)
    TextInputLayout textInputLayout1;
    @BindView(R.id.loginTextInputLayout2)
    TextInputLayout textInputLayout2;
    @BindView(R.id.loginButton)
    Button button;
    @BindView(R.id.loginToolbar)
    Toolbar toolbar;
    @BindView(R.id.loginCoordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.loginProgressBar)
    ProgressBar progressBar;

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case -1:
                    button.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(coordinatorLayout, msg.obj.toString(), Snackbar.LENGTH_SHORT);
                    break;
                case 0:
                    SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setSupportActionBar(toolbar);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void login() {
        final String account = textInputLayout1.getEditText().getText().toString().trim();
        final String password = textInputLayout2.getEditText().getText().toString().trim();
        button.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        if (!(account.equals("") || password.equals(""))) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        LoginHelper.getSchedule(account, password);
                    } catch (Exception e) {
                        Message message = new Message();
                        message.obj = e;
                        message.what = -1;
                        handler.sendMessage(message);
                    }
                    handler.sendEmptyMessage(0);
                }
            }).start();
        }
    }
}
